package jm.droid.lib.taskchain

internal interface ITask {
    /**
     * 任务的执行入口，每个任务都由此进入开始
     * @param chain
     */
    fun work(chain: AbsTask.Chain)

    /**
     * 任务链取消时，当前正在work的任务才会执行此方法。可以在这里作一些回收资源的操作
     * @param call
     */
    fun onCancel(call: Call)

    /**
     * 此任务的一些描述，或者一些附带信息
     */
    fun describe(): Any? = null

    /**
     * 如果是任务设置了超时时间并且在指定时间内未完成，则回调此方法
     * @return true 继续走下一个任务， false 中断任务链
     */
    fun onTimeOut() = true
}
