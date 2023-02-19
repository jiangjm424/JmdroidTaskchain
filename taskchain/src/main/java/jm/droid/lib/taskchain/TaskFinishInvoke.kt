package jm.droid.lib.taskchain

internal class TaskFinishInvoke : AbsTask() {
    override fun work(chain: Chain) {
        (chain.call() as? CallImpl)?.done()
        chain.listener()?.onFinish(chain.call())
    }

    override fun describe(): Any? = null
}
