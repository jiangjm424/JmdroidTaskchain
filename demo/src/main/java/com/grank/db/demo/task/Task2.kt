package com.grank.db.demo.task

import android.util.Log
import jm.droid.lib.taskchain.AbsTask
import jm.droid.lib.taskchain.Call
import jm.droid.lib.taskchain.ExecuteOn

class Task2 : AbsTask() {
    override fun work(chain: Chain) {
        Log.i("jiang","run ${this.javaClass} e")
        Log.i("jiang","run ${this.javaClass} x")
        nextTask(chain.request())
    }

    override fun onCancel(call: Call) {

        executeOn(ExecuteOn.MAIN) {

        }
    }
}
