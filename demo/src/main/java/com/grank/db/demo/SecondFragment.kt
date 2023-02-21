package com.grank.db.demo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.grank.db.demo.databinding.FragmentSecondBinding
import com.grank.db.demo.dialog.D1
import com.grank.db.demo.dialog.D2
import com.grank.db.demo.dialog.D3
import com.grank.db.demo.dialog.D4
import com.grank.db.demo.dialog.D5
import jm.droid.lib.taskchain.Call
import jm.droid.lib.taskchain.ExecuteOn
import jm.droid.lib.taskchain.Request

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class SecondFragment : Fragment() {

    private var _binding: FragmentSecondBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val vvv by viewModels<SecondRemodel>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentSecondBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonSecond.setOnClickListener {
            findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
        }
        binding.buttonFresh.setOnClickListener {
            Call.Builder(requireContext())
                .addTask(D1())
                .addTasks(listOf(D2(), D3(), D4(), D5()))
                .setRequest(Request())
                .build().execute(ExecuteOn.MAIN)
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
