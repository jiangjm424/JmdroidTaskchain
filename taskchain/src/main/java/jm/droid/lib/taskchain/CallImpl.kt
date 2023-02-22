package jm.droid.lib.taskchain

import android.content.Context
import java.util.concurrent.atomic.AtomicBoolean

internal class CallImpl(
    private val context: Context?,
    private val request: Request,
    private val tasks: List<AbsTask>,
    private val listener: TaskchainListener?,
) : Call {

    private val executed = AtomicBoolean()
    private val canceled = AtomicBoolean()
    private val callDone = AtomicBoolean()

    override fun execute(dispatchers: ExecuteOn) {
        check(executed.compareAndSet(false, true)) { "Already Executed" }
        getRealChainResponse(dispatchers)
    }

    override fun cancel() {
        if (callDone.get() || canceled.get()) return
        canceled.compareAndSet(false, true)
        val task = tasks.firstOrNull { it.isWorking() }
        task?.cancel(this)
        listener?.onCanceled(this, task)
    }

    /**
     * @param finish true 任务链所有任务执行完成后结束  false  任务链被某个任务中断或者超时导致的任务结
     */
    internal fun done(finish: Boolean) {
        callDone.compareAndSet(false, true)
    }

    private fun getRealChainResponse(dispatchers: ExecuteOn) {
        if (canceled.get()) return
        listener?.onStart(this)
        val taskChain = mutableListOf<AbsTask>()
        taskChain.add(TaskExecuteOn(dispatchers))
        taskChain.addAll(tasks)
        taskChain.add(TaskFinishInvoke())
        val chain = ChainImpl(context, taskChain, 0, request, listener, this)
        chain.process(request)
    }
}
