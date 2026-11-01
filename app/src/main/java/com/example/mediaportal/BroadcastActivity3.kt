package com.example.mediaportal

import android.os.Bundle
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import com.example.mediaportal.databinding.ActivityBroadcast3Binding

class BroadcastActivity3 : AppCompatActivity() {
    private lateinit var binding: ActivityBroadcast3Binding
    private var selection = BroadcastReceiverFragment.OPTION_CUSTOM
    private var textToBroadcast: String? = null

    companion object {
        const val ACTION_CUSTOM_BROADCAST = "com.example.mediaportal.CUSTOM_BROADCAST"
        const val EXTRA_MESSAGE = "message"
    }

    private val customReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == ACTION_CUSTOM_BROADCAST) {
                val message = intent.getStringExtra(EXTRA_MESSAGE) ?: "No message extra found"
                binding.tvReceivedMessage.text = "Received Broadcast Message:\n\n\"$message\""
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBroadcast3Binding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        selection = intent.getIntExtra(
            BroadcastReceiverFragment.EXTRA_SELECTION,
            BroadcastReceiverFragment.OPTION_CUSTOM
        )
        textToBroadcast = intent.getStringExtra(EXTRA_MESSAGE)

        if (selection == BroadcastReceiverFragment.OPTION_CUSTOM) {
            binding.tvReceivedMessage.text = "Registering Broadcast Receiver..."
        } else {
            binding.tvReceivedMessage.visibility = android.view.View.GONE
            binding.customBroadcastContent.visibility = android.view.View.GONE
        }
    }

    override fun onStart() {
        super.onStart()
        if (selection == BroadcastReceiverFragment.OPTION_CUSTOM) {
            val filter = IntentFilter(ACTION_CUSTOM_BROADCAST)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                registerReceiver(customReceiver, filter, RECEIVER_NOT_EXPORTED)
            } else {
                registerReceiver(customReceiver, filter)
            }
            textToBroadcast?.let { message ->
                sendBroadcast(Intent(ACTION_CUSTOM_BROADCAST).apply {
                    putExtra(EXTRA_MESSAGE, message)
                    setPackage(packageName)
                })
            }
        }
    }

    override fun onStop() {
        if (selection == BroadcastReceiverFragment.OPTION_CUSTOM) {
            try { unregisterReceiver(customReceiver) } catch (_: IllegalArgumentException) { }
        }
        super.onStop()
    }
}
