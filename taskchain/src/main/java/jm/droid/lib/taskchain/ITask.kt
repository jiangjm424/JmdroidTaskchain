package jm.droid.lib.taskchain

internal interface ITask {
    fun work(chain: AbsTask.Chain)
    fun describe(): Any?
}
