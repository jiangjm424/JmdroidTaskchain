package com.grank.db.demo.task

import android.util.Log
import jm.droid.lib.taskchain.AbsTask

class Task5 : AbsTask() {
    override fun work(chain: Chain) {
        Log.i("jiang","run ${this.javaClass} e")
        Log.i("jiang","run ${this.javaClass} x")
        nextTask(chain.request())
    }

    override fun describe(): Any = this.javaClass
}
