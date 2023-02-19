package jm.droid.lib.taskchain

import android.content.Context

abstract class AbsTask : ITask {
    private var nextChain: Chain? = null
    internal fun attachNextChain(chain: Chain) {
        this.nextChain = chain
    }

    internal fun isWorking(): Boolean = nextChain != null

    internal fun cancel() {
        nextChain = null
    }

    fun nextTask(req: Request) {
        val c = nextChain ?: return
        nextChain = null
        c.process(req)
    }

    fun interrupt(code: Int, msg: String?) {
        val c = nextChain ?: return
        nextChain = null
        c.listener()?.onInterrupt(this, code, msg)
    }

    interface Chain {
        fun context(): Context?
        fun request(): Request
        fun call(): Call
        fun listener(): TaskListener?
        fun process(req: Request)
    }
}
