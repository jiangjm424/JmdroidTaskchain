package jm.droid.lib.taskchain

class TaskExecuteOn(private val executeOn: ExecuteOn) : AbsTask() {
    override fun work(chain: Chain) {
        when (executeOn) {
            ExecuteOn.MAIN -> Dispatcher.M.post { nextTask(chain.request()) }
            ExecuteOn.IO -> Dispatcher.I.execute { nextTask(chain.request()) }
            else -> nextTask(chain.request())
        }
    }

    override fun describe(): Any? {
        return "a task that switch the thread when next task execute on"
    }
    override fun onCancel(call: Call) {
    }
}
