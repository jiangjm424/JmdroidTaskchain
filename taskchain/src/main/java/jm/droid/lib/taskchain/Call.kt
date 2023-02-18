package jm.droid.lib.taskchain

import android.content.Context

interface Call {
    fun execute(sync: Boolean)
    class Builder(private val context: Context? = null) {
        private var request: Request? = null
        private var tasks: MutableList<AbsTask> = mutableListOf()
        private var listener: TaskListener? = null
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

        fun setListener(listener: TaskListener?): Builder {
            this.listener = listener
            return this
        }

        fun build(): Call {
            return CallImpl(context, request!!, tasks, listener)
        }

    }
}
