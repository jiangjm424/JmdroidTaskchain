package jm.droid.lib.taskchain

import android.content.Context
import java.util.concurrent.TimeUnit

abstract class AbsTask(private val timeout: Long = 0) : AsyncTimeout(), ITask {
    private var nextChain: Chain? = null
    internal fun attachNextChain(chain: Chain) {
        this.nextChain = chain
        enterTimeoutIfNeeded()
    }

    override fun timedOut() {
        val t = onTimeOut()
        if (t) {
            nextTask(nextChain?.request() ?: Request("timeOut"))
        } else {
            interrupt(0, "time out")
        }
    }

    internal fun isWorking(): Boolean = nextChain != null

    //任务链被取消时由chain调用
    internal fun cancel(call: Call) {
        nextChain = null
        exitTimeoutIfNeeded()
        onCancel(call)
    }

    /**
     * 当前任务执行完成后，执行下一个任务
     */
    fun nextTask(req: Request) {
        exitTimeoutIfNeeded()
        val c = nextChain ?: return
        nextChain = null
        c.process(req)
    }

    /**
     * 中为后续任务链中后面任务的执行
     * @param code 外部业务请使用大于0的数， 库内部出现的中断会使用小于等于0的code
     *             0 表示任务出现超时后的中断code
     * @param msg  中断的原因描述
     */
    fun interrupt(code: Int, msg: String?) {
        exitTimeoutIfNeeded()
        val c = nextChain ?: return
        nextChain = null
        (c.call() as CallImpl).done(false)
        c.listener()?.onInterrupt(this, code, msg)
    }

    /**
     * 方便任务执行时切换线程
     */
    fun executeOn(executeOn: ExecuteOn, block: () -> Unit) {
        when (executeOn) {
            ExecuteOn.MAIN -> Dispatcher.M.post { block() }
            ExecuteOn.IO -> Dispatcher.I.execute { block() }
            else -> block()
        }
    }

    private fun enterTimeoutIfNeeded() {
        if (timeout <= 0) return
        timeout(timeout, TimeUnit.MILLISECONDS)
        enter()
    }

    private fun exitTimeoutIfNeeded() {
        if (timeout <= 0) return
        exit()
    }

    interface Chain {
        fun context(): Context?
        fun request(): Request
        fun call(): Call
        fun listener(): TaskchainListener?
        fun process(req: Request)
    }
}
