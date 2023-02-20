# 任务链串行实现

> 最近在应用弹框里面出现进入应用时弹出多个对话框导致界面不美观的问题，于是就想着有没有什么办法来让弹框按顺序执行下去。参考了网络上较多的方法，感觉使用责任链的模式来实现比较符合我们的需求。
> 因此这里我们设计了更加方便使用的任务链执行逻辑使用起来也相当方便。
>

### 1 引入库

```groovy
implementation 'io.github.jiangjm424:taskchain:+'
```

### 2 创建任务
**注意这里记得当任务执行完成后，如果需要继续下一个任务，则需要调用nextTask(req), 或者要中断继续执行，也可以直接调用 interrupt(code,msg)**

```kotlin
class Task1 : AbsTask() {
    override fun work(chain: Chain) {
        Log.i(TAG, "run ${this.javaClass} e")
        Log.i(TAG, "run ${this.javaClass} x")
        nextTask(chain.request())
    }

    override fun describe(): Any = this.javaClass
}
```

### 3 创建任务链
**我们还可以为任务链创建监听对象，用于查看本次任务链的执行情况。请注意，每个任务链只能执行execute(sync)
一次，当有任务依赖外部条件时，此时的调用线程会成为后续任务的运行线程，所以这里我们可以添加一些切换后面任务运动线程的任务**

```kotlin
private val task3 = Task3()  //task3需要依赖外部条件满足后才可以继续执行后续任务或者中断任务链
val t = Call.Builder().setRequest(Request()).addTask(Task1())
    .addTasks(listOf(Task2(), task3, Task4(), Task5()))
    .setListener(object : TaskListener {
        override fun onStart(call: Call) {
            Log.i(TAG, "task call start :$call")
        }
        override fun onFinish(call: Call) {
            Log.i(TAG, "task call finish :$call")
        }

        override fun onTaskExecute(ii: AbsTask, index: Int) {
            Log.i(TAG, "task execute :${ii.describe()}, index:$index")
        }

        override fun onInterrupt(ii: AbsTask, code: Int, errMsg: String?) {
            Log.i(TAG, "task interrupt :$ii, code:$code, msg:$errMsg")
        }

    })
    .build()
t.execute(true)
```

== 对话框按序展示请参考 SecondFragment 中的示例 ==
#### 1 先实现对话框的的任务实现
```kotlin
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

    override fun describe(): Any? {
        return null
    }
}
```

#### 2 使用taskchain将对话框任务串联起来

```kotlin
Call.Builder(requireContext()).addTask(D1()).addTasks(listOf(D2(), D3(), D4(), D5()))
    .build().execute(true) //由于是ui相关，直接在主线程运行
```
