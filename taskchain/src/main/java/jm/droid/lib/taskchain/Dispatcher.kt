package jm.droid.lib.taskchain

import android.os.Handler
import android.os.Looper
import java.util.concurrent.SynchronousQueue
import java.util.concurrent.ThreadFactory
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

internal object Dispatcher {
    val M by lazy { Handler(Looper.getMainLooper(), null) }
    val I by lazy {
        ThreadPoolExecutor(0, Int.MAX_VALUE,
            60, TimeUnit.SECONDS,
            SynchronousQueue(),
            threadFactory("Taskchain-Dispatcher", false))
    }

    private fun threadFactory(
        name: String,
        daemon: Boolean
    ): ThreadFactory = ThreadFactory { runnable ->
        Thread(runnable, name).apply {
            isDaemon = daemon
        }
    }

}

sealed class ExecuteOn {
    object MAIN : ExecuteOn()
    object IO : ExecuteOn()
    object DEFAULT : ExecuteOn()
}
