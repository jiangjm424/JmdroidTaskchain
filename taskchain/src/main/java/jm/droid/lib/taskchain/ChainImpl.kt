package jm.droid.lib.taskchain

import android.content.Context

internal class ChainImpl(
    private val context: Context?,
    private val taskChain: List<AbsTask>,
    private val index: Int,
    private val originalRequest: Request,
    private val listener: TaskchainListener?,
    private val call: Call,
) : AbsTask.Chain {
    override fun request(): Request = originalRequest
    override fun call(): Call = call
    override fun context(): Context? = context
    override fun listener(): TaskchainListener? = listener

    override fun process(req: Request) {
        val c = taskChain[index]
        if (index != 0 && index < taskChain.size - 1) {
            listener?.onTaskExecute(c, index)
        }
        val chain = copy(index = index + 1, req = req)
        c.attachNextChain(chain)
        c.work(chain)
    }

    private fun copy(
        context: Context? = this.context,
        taskChain: List<AbsTask> = this.taskChain,
        index: Int = this.index,
        req: Request = this.originalRequest,
        listener: TaskchainListener? = this.listener,
        call: Call = this.call
    ): ChainImpl = ChainImpl(context, taskChain, index, req, listener, call)
}
