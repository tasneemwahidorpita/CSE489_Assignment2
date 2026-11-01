package com.example.mediaportal

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.mediaportal.databinding.ActivityBroadcast2Binding

class BroadcastActivity2 : AppCompatActivity() {

    private lateinit var binding: ActivityBroadcast2Binding
    private var selection = BroadcastReceiverFragment.OPTION_CUSTOM

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            updateBatteryStatus(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBroadcast2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        selection = intent.getIntExtra(
            BroadcastReceiverFragment.EXTRA_SELECTION,
            BroadcastReceiverFragment.OPTION_CUSTOM
        )

        if (selection == BroadcastReceiverFragment.OPTION_CUSTOM) {
            binding.cardCustomBroadcast.visibility = View.VISIBLE
            binding.cardBatteryReceiver.visibility = View.GONE

            binding.btnProceedCustom.setOnClickListener {
                val message = binding.etMessage.text.toString().trim()
                if (message.isEmpty()) {
                    binding.tilMessage.error = getString(R.string.error_empty_message)
                } else {
                    binding.tilMessage.error = null
                    val nextIntent = Intent(this, BroadcastActivity3::class.java).apply {
                        putExtra(BroadcastReceiverFragment.EXTRA_SELECTION, selection)
                        putExtra("message", message)
                    }
                    startActivity(nextIntent)
                }
            }
        } else {
            binding.cardCustomBroadcast.visibility = View.GONE
            binding.cardBatteryReceiver.visibility = View.VISIBLE

            binding.btnProceedBattery.setOnClickListener {
                val nextIntent = Intent(this, BroadcastActivity3::class.java).apply {
                    putExtra(BroadcastReceiverFragment.EXTRA_SELECTION, selection)
                }
                startActivity(nextIntent)
            }
        }
    }

    private fun updateBatteryStatus(intent: Intent?) {
        intent?.let {
            if (it.action == Intent.ACTION_BATTERY_CHANGED) {
                val level = it.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale = it.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                if (level != -1 && scale != -1) {
                    val batteryPct = (level * 100 / scale.toFloat()).toInt()
                    binding.tvBatteryPercentage.text = "$batteryPct%"
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (selection == BroadcastReceiverFragment.OPTION_BATTERY) {
            val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            // ACTION_BATTERY_CHANGED is a sticky broadcast. Registering returns current status immediately.
            val stickyStatus = registerReceiver(batteryReceiver, filter)
            updateBatteryStatus(stickyStatus)
        }
    }

    override fun onStop() {
        super.onStop()
        if (selection == BroadcastReceiverFragment.OPTION_BATTERY) {
            try {
                unregisterReceiver(batteryReceiver)
            } catch (e: Exception) {
                // Receiver was not registered
            }
        }
    }
}
