package jm.droid.lib.taskchain

import android.content.Context
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

internal class CallImpl(
    private val context: Context?,
    private val request: Request,
    private val tasks: List<AbsTask>,
    private val listener: TaskListener?,
) : Call {
    companion object {
        private val executeService = Executors.newSingleThreadExecutor()
    }

    private val executed = AtomicBoolean()

    override fun execute(sync: Boolean) {
        check(executed.compareAndSet(false, true)) { "Already Executed" }
        if (sync) getRealChainResponse()
        else executeService.execute { getRealChainResponse() }
    }

    private fun getRealChainResponse() {
        listener?.onStart(this)
        val taskChain = mutableListOf<AbsTask>()
        taskChain.addAll(tasks)
        taskChain.add(TaskFinishInvoke())
        val chain = ChainImpl(context, taskChain, 0, request, listener, this)
        chain.process(request)
    }
}
