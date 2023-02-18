package com.grank.db.demo.task

import android.util.Log
import jm.droid.lib.taskchain.AbsTask
import jm.droid.lib.taskchain.Request

class Task3 : AbsTask() {
    override fun work(chain: Chain) {
        Log.i("jiang", "run ${this.javaClass} e")
        Log.i("jiang", "run ${this.javaClass} x")
//        nextTask(chain.request())
    }

    fun ready(r: Boolean) {
        if (r)
            nextTask(Request())
        else
            interrupt(3, "aa")
    }

    override fun describe(): Any = this.javaClass
}
