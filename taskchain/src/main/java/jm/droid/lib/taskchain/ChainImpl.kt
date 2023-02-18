package jm.droid.lib.taskchain

import android.content.Context

internal class ChainImpl(
    private val context: Context?,
    private val taskChain: List<AbsTask>,
    private val index: Int,
    private val originalRequest: Request,
    private val listener: TaskListener?,
    private val call: Call,
) : AbsTask.Chain {
    override fun request(): Request = originalRequest
    override fun call(): Call = call
    override fun context(): Context? = context
    override fun listener(): TaskListener? = listener

    override fun process(req: Request) {
        val c = taskChain[index]
        val chain = copy(index = index + 1)
        c.attachNextChain(chain)
        if (index < taskChain.size - 1) {
            listener?.onTaskExecute(c, index)
        }
        c.work(chain)
    }

    private fun copy(
        context: Context? = this.context,
        taskChain: List<AbsTask> = this.taskChain,
        index: Int = this.index,
        req: Request = this.originalRequest,
        listener: TaskListener? = this.listener,
        call: Call = this.call
    ): ChainImpl = ChainImpl(context, taskChain, index, req, listener, call)
}
