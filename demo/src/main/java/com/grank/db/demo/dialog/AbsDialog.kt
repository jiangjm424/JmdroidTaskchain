package com.grank.db.demo.dialog

import android.app.AlertDialog
import com.grank.db.demo.R
import jm.droid.lib.taskchain.AbsTask
import jm.droid.lib.taskchain.Call
import jm.droid.lib.taskchain.Request

abstract class AbsDialog : AbsTask() {
    abstract val title: String
    abstract val message: String
    override fun work(chain: Chain) {
        AlertDialog.Builder(chain.context()).setIcon(R.drawable.ic_launcher_foreground)
            .setTitle(title).setMessage(message).setPositiveButton("next") { _, _ ->
                nextTask(Request(describe()))
            }.setNegativeButton("中断") { _, _ ->
                interrupt(1, "interrupt")
            }.create().show()

    }

    override fun onCancel(call: Call) {

    }
}
