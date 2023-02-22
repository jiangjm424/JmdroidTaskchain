package jm.droid.lib.taskchain

interface TaskchainListener {
    /**
     * 任务链开始执行
     */
    fun onStart(call: Call)

    /**
     * 任务链执行完成
     */
    fun onFinish(call: Call)

    /**
     * 取消任务链调用后的回调  若任务链还未开始执行，则 @param task 为空
     */
    fun onCanceled(call: Call, task: AbsTask?)

    /**
     * 开始运行任务
     */
    fun onTaskExecute(task: AbsTask, index: Int)

    /**
     * 由任务主动中断后续任务时触发
     */
    fun onInterrupt(task: AbsTask, code: Int, errMsg: String?)
}
