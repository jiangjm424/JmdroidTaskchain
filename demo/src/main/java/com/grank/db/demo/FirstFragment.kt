package com.grank.db.demo

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.grank.db.demo.databinding.FragmentFirstBinding
import com.grank.db.demo.task.Task1
import com.grank.db.demo.task.Task2
import com.grank.db.demo.task.Task3
import com.grank.db.demo.task.Task4
import com.grank.db.demo.task.Task5
import jm.droid.lib.taskchain.AbsTask
import jm.droid.lib.taskchain.Call
import jm.droid.lib.taskchain.ExecuteOn
import jm.droid.lib.taskchain.Request
import jm.droid.lib.taskchain.TaskExecuteOn
import jm.droid.lib.taskchain.TaskListener

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root

    }

    //    private val LIULISHUO_APK_URL = "https://dl0002.liqucn.com/0e74524526a594fc0d692d9830f9228d/63ccfa7f/upload/2021/310/h/com.huawei.health_12.0.11.300_liqucn.com.apk"
//    private val LIULISHUO_APK_URL = "https://tse4-mm.cn.bing.net/th/id/OIP-C.2EbOk5g-nxqS0gja5pBgfAHaEC?pid=ImgDet&rs=1"
    private val LIULISHUO_APK_URL =
        "http://110.81.196.233:49155/down.qq.com/xunxian/patch/ManualPatch4.5.5.1-4.5.7.1-SD.exe?mkey=63ce31b6e7ed56832482e0fce9c4779b&arrive_key=26595237213&cip=116.25.41.233&proto=http&access_type="

    private val task3 = Task3()
    private var call:Call?=null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireContext().getExternalFilesDir(null)?.absolutePath
        binding.buttonFirst.setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }
        binding.startDownload.setOnClickListener {
            call = Call.Builder().setRequest(Request()).addTask(Task1())
                .addTasks(listOf(Task2(), task3, TaskExecuteOn(ExecuteOn.IO),Task4(), TaskExecuteOn(ExecuteOn.MAIN),Task5()))
                .setListener(object :TaskListener{
                    override fun onStart(call: Call) {
                        Log.i("jiang","task call start :$call")
                    }
                    override fun onFinish(call: Call) {
                        Log.i("jiang","task call finish :$call")
                    }

                    override fun onCanceled(call: Call, task: AbsTask?) {
                        Log.i("jiang","task call cancel :$call, task:$task")
                    }
                    override fun onTaskExecute(task: AbsTask, index: Int) {
                        Log.i("jiang","task execute :${task.describe()}, index:$index")
                    }

                    override fun onInterrupt(ii: AbsTask, code: Int, errMsg: String?) {
                        Log.i("jiang","task interrupt :$ii, code:$code, msg:$errMsg")
                    }

                })
                .build()
            call?.execute(ExecuteOn.IO)
        }
        binding.startIdDownload.setOnClickListener {
            task3.ready(true)
        }
        binding.stopIdDownload.setOnClickListener {
            task3.ready(false)
        }
        binding.stopDownload.setOnClickListener {
            call?.cancel()
        }
//        binding.pauseAllDownload.setOnClickListener {
//        }
//        binding.removeAllDownload.setOnClickListener {
//        }
//        binding.resumeAllDownload.setOnClickListener {
//        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
