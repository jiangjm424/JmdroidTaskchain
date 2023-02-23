package jm.droid.lib.taskchain

import android.content.Context

interface Call {
    /**
     * 任务链接开始运行
     * @param dispatchers 任务链开始时运行的线程，当然任务也可以使用 executeOn(ExecuteOn,block)进行切换
     */
    fun execute(dispatchers: ExecuteOn=ExecuteOn.DEFAULT)

    /**
     * 取消任务链的执行，若当前有正在执行的任务，则会回调该任务的 onCancel 方法
     */
    fun cancel()
    class Builder(private val context: Context? = null) {
        private var request: Request = Request()
        private var tasks: MutableList<AbsTask> = mutableListOf()
        private var listener: TaskchainListener? = null
        fun setRequest(request: Request): Builder {
            this.request = request
            return this
        }

        fun addTask(task: AbsTask): Builder {
            tasks.add(task)
            return this
        }

        fun addTasks(tasks: List<AbsTask>): Builder {
            this.tasks.addAll(tasks)
            return this
        }

        fun setListener(listener: TaskchainListener?): Builder {
            this.listener = listener
            return this
        }

        fun build(): Call {
            return CallImpl(context, request, tasks, listener)
        }

    }
}
