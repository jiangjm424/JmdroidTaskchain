package jm.droid.lib.taskchain

import java.io.IOException
import java.io.InterruptedIOException
import java.util.concurrent.TimeUnit

/**
 * This timeout uses a background thread to take action exactly when the timeout occurs. Use this to
 * implement timeouts where they aren't supported natively, such as to sockets that are blocked on
 * writing.
 *
 * Subclasses should override [timedOut] to take action when a timeout occurs. This method will be
 * invoked by the shared watchdog thread so it should not do any long-running operations. Otherwise
 * we risk starving other timeouts from being triggered.
 *
 * Use [sink] and [source] to apply this timeout to a stream. The returned value will apply the
 * timeout to each operation on the wrapped stream.
 *
 * Callers should call [enter] before doing work that is subject to timeouts, and [exit] afterwards.
 * The return value of [exit] indicates whether a timeout was triggered. Note that the call to
 * [timedOut] is asynchronous, and may be called after [exit].
 */
open class AsyncTimeout : Timeout() {
    /** True if this node is currently in the queue.  */
    private var inQueue = false

    /** The next node in the linked list.  */
    private var next: AsyncTimeout? = null

    /** If scheduled, this is the time that the watchdog should time this out.  */
    private var timeoutAt = 0L

    fun enter() {
        check(!inQueue) { "Unbalanced enter/exit" }
        val timeoutNanos = timeoutNanos()
        val hasDeadline = hasDeadline()
        if (timeoutNanos == 0L && !hasDeadline) {
            return // No timeout and no deadline? Don't bother with the queue.
        }
        inQueue = true
        scheduleTimeout(this, timeoutNanos, hasDeadline)
    }

    /** Returns true if the timeout occurred.  */
    fun exit(): Boolean {
        if (!inQueue) return false
        inQueue = false
        return cancelScheduledTimeout(this)
    }

    /**
     * Returns the amount of time left until the time out. This will be negative if the timeout has
     * elapsed and the timeout should occur immediately.
     */
    private fun remainingNanos(now: Long) = timeoutAt - now

    /**
     * Invoked by the watchdog thread when the time between calls to [enter] and [exit] has exceeded
     * the timeout.
     */
    protected open fun timedOut() {}

    private class Watchdog : Thread("Taskchain-Watchdog") {
        init {
            isDaemon = true
        }

        override fun run() {
            while (true) {
                try {
                    var timedOut: AsyncTimeout? = null
                    synchronized(AsyncTimeout::class.java) {
                        timedOut = awaitTimeout()

                        // The queue is completely empty. Let this thread exit and let another watchdog thread
                        // get created on the next call to scheduleTimeout().
                        if (timedOut === head) {
                            head = null
                            return
                        }
                    }

                    // Close the timed out node, if one was found.
                    timedOut?.timedOut()
                } catch (ignored: InterruptedException) {
                }
            }
        }
    }

    companion object {
        /**
         * Don't write more than 64 KiB of data at a time, give or take a segment. Otherwise slow
         * connections may suffer timeouts even when they're making (slow) progress. Without this,
         * writing a single 1 MiB buffer may never succeed on a sufficiently slow connection.
         */
        private const val TIMEOUT_WRITE_SIZE = 64 * 1024

        /** Duration for the watchdog thread to be idle before it shuts itself down.  */
        private val IDLE_TIMEOUT_MILLIS = TimeUnit.SECONDS.toMillis(60)
        private val IDLE_TIMEOUT_NANOS = TimeUnit.MILLISECONDS.toNanos(IDLE_TIMEOUT_MILLIS)

        /**
         * The watchdog thread processes a linked list of pending timeouts, sorted in the order to be
         * triggered. This class synchronizes on AsyncTimeout.class. This lock guards the queue.
         *
         * Head's 'next' points to the first element of the linked list. The first element is the next
         * node to time out, or null if the queue is empty. The head is null until the watchdog thread
         * is started and also after being idle for [AsyncTimeout.IDLE_TIMEOUT_MILLIS].
         */
        private var head: AsyncTimeout? = null

        private fun scheduleTimeout(node: AsyncTimeout, timeoutNanos: Long, hasDeadline: Boolean) {
            synchronized(AsyncTimeout::class.java) {
                // Start the watchdog thread and create the head node when the first timeout is scheduled.
                if (head == null) {
                    head = AsyncTimeout()
                    Watchdog().start()
                }

                val now = System.nanoTime()
                if (timeoutNanos != 0L && hasDeadline) {
                    // Compute the earliest event; either timeout or deadline. Because nanoTime can wrap
                    // around, minOf() is undefined for absolute values, but meaningful for relative ones.
                    node.timeoutAt = now + minOf(timeoutNanos, node.deadlineNanoTime() - now)
                } else if (timeoutNanos != 0L) {
                    node.timeoutAt = now + timeoutNanos
                } else if (hasDeadline) {
                    node.timeoutAt = node.deadlineNanoTime()
                } else {
                    throw AssertionError()
                }

                // Insert the node in sorted order.
                val remainingNanos = node.remainingNanos(now)
                var prev = head!!
                while (true) {
                    if (prev.next == null || remainingNanos < prev.next!!.remainingNanos(now)) {
                        node.next = prev.next
                        prev.next = node
                        if (prev === head) {
                            // Wake up the watchdog when inserting at the front.
                            (AsyncTimeout::class.java as Object).notify()
                        }
                        break
                    }
                    prev = prev.next!!
                }
            }
        }

        /** Returns true if the timeout occurred. */
        private fun cancelScheduledTimeout(node: AsyncTimeout): Boolean {
            synchronized(AsyncTimeout::class.java) {
                // Remove the node from the linked list.
                var prev = head
                while (prev != null) {
                    if (prev.next === node) {
                        prev.next = node.next
                        node.next = null
                        return false
                    }
                    prev = prev.next
                }

                // The node wasn't found in the linked list: it must have timed out!
                return true
            }
        }

        /**
         * Removes and returns the node at the head of the list, waiting for it to time out if
         * necessary. This returns [head] if there was no node at the head of the list when starting,
         * and there continues to be no node after waiting [IDLE_TIMEOUT_NANOS]. It returns null if a
         * new node was inserted while waiting. Otherwise this returns the node being waited on that has
         * been removed.
         */
        @Throws(InterruptedException::class)
        internal fun awaitTimeout(): AsyncTimeout? {
            // Get the next eligible node.
            val node = head!!.next

            // The queue is empty. Wait until either something is enqueued or the idle timeout elapses.
            if (node == null) {
                val startNanos = System.nanoTime()
                (AsyncTimeout::class.java as Object).wait(IDLE_TIMEOUT_MILLIS)
                return if (head!!.next == null && System.nanoTime() - startNanos >= IDLE_TIMEOUT_NANOS) {
                    head // The idle timeout elapsed.
                } else {
                    null // The situation has changed.
                }
            }

            var waitNanos = node.remainingNanos(System.nanoTime())

            // The head of the queue hasn't timed out yet. Await that.
            if (waitNanos > 0) {
                // Waiting is made complicated by the fact that we work in nanoseconds,
                // but the API wants (millis, nanos) in two arguments.
                val waitMillis = waitNanos / 1000000L
                waitNanos -= waitMillis * 1000000L
                (AsyncTimeout::class.java as Object).wait(waitMillis, waitNanos.toInt())
                return null
            }

            // The head of the queue has timed out. Remove it.
            head!!.next = node.next
            node.next = null
            return node
        }
    }
}

