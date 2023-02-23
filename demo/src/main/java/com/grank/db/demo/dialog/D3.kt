package com.grank.db.demo.dialog

import jm.droid.lib.taskchain.Request

class D3 : AbsDialog(4000) {
    override val title: String
        get() = "title:" + this.javaClass.canonicalName
    override val message: String
        get() = "message:" + this.javaClass.javaClass

    override fun onTimeOut(): Boolean {
        nextTask(Request())
        return true
    }
}
