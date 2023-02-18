package jm.droid.lib.taskchain

class TaskFinishInvoke : AbsTask() {
    override fun work(chain: Chain) {
        chain.listener()?.onFinish(chain.call())
    }

    override fun describe(): Any? = null
}
