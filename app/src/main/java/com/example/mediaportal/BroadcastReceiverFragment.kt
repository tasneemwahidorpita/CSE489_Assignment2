package com.example.mediaportal

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import com.example.mediaportal.databinding.FragmentBroadcastReceiverBinding

class BroadcastReceiverFragment : Fragment() {

    private var _binding: FragmentBroadcastReceiverBinding? = null
    private val binding get() = _binding!!

    companion object {
        const val EXTRA_SELECTION = "extra_broadcast_selection"
        const val OPTION_CUSTOM = 0
        const val OPTION_BATTERY = 1
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBroadcastReceiverBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val options = arrayOf(
            getString(R.string.option_custom_broadcast),
            getString(R.string.option_battery_receiver)
        )

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            options
        )
        binding.spinnerBroadcastType.adapter = adapter

        binding.btnProceed.setOnClickListener {
            val selectedPosition = binding.spinnerBroadcastType.selectedItemPosition
            val intent = Intent(requireContext(), BroadcastActivity2::class.java).apply {
                putExtra(EXTRA_SELECTION, selectedPosition)
            }
            startActivity(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
