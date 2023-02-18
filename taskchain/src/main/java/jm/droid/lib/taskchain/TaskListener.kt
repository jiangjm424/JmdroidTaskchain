package jm.droid.lib.taskchain

interface TaskListener {
    fun onStart(call: Call)
    fun onFinish(call: Call)
    fun onTaskExecute(ii: AbsTask, index: Int)
    fun onInterrupt(ii: AbsTask, code: Int, errMsg: String?)
}
