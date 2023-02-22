package com.grank.db.demo.task

import android.util.Log
import jm.droid.lib.taskchain.AbsTask
import jm.droid.lib.taskchain.Call
import jm.droid.lib.taskchain.Request

class Task3 : AbsTask(5000L) {
    override fun work(chain: Chain) {
        Log.i("jiang", "run ${this.javaClass} e")
        Log.i("jiang", "run ${this.javaClass} x")
//        nextTask(chain.request())
    }

    override fun onTimeOut(): Boolean {
        Log.i("jiangg", "task 3 time out")
        return false
    }

    fun ready(r: Boolean) {
        if (r)
            nextTask(Request())
        else
            interrupt(3, "aa")
    }

    override fun onCancel(call: Call) {
        Log.i("jiang", "onCancel this:$this call:$call")
    }
}
