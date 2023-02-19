package com.grank.db.demo.dialog

class D4 : AbsDialog() {
    override val title: String
        get() = "title:" + this.javaClass.canonicalName
    override val message: String
        get() = "message:" + this.javaClass.javaClass
}
