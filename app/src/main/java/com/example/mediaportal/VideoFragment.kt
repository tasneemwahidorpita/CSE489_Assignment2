package com.example.mediaportal

import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import com.example.mediaportal.databinding.FragmentVideoBinding
import java.util.concurrent.TimeUnit

class VideoFragment : Fragment() {
    private var _binding: FragmentVideoBinding? = null
    private val binding get() = _binding!!
    private val handler = Handler(Looper.getMainLooper())
    private var prepared = false
    private var dragging = false
    private var durationMs = 0
    private var savedPositionMs = 0
    private var savedIsPlaying = false

    private val updateProgress = object : Runnable {
        override fun run() {
            val b = _binding ?: return
            if (prepared) {
                val position = b.videoView.currentPosition.coerceIn(0, durationMs)
                if (!dragging) b.seekbarVideo.progress = position
                showTimes(position, durationMs)
                if (b.videoView.isPlaying) handler.postDelayed(this, 250)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentVideoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        savedPositionMs = savedInstanceState?.getInt("saved_video_pos", 0) ?: 0
        savedIsPlaying = savedInstanceState?.getBoolean("saved_video_playing", false) ?: false

        binding.btnPlayPause.setOnClickListener { togglePlayback() }
        binding.seekbarVideo.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar?) { dragging = true }
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser && prepared) showTimes(progress, durationMs)
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                dragging = false
                if (prepared) {
                    val target = seekBar?.progress ?: 0
                    binding.videoView.seekTo(target)
                    showTimes(target, durationMs)
                    handler.removeCallbacks(updateProgress)
                    handler.post(updateProgress)
                }
            }
        })

        val videoId = resources.getIdentifier("sample_video", "raw", requireContext().packageName)
        if (videoId == 0) {
            binding.progressBar.visibility = View.GONE
            binding.cardFileNotice.visibility = View.VISIBLE
            return
        }
        binding.cardFileNotice.visibility = View.GONE
        binding.progressBar.visibility = View.VISIBLE
        binding.videoView.setOnPreparedListener { player ->
            prepared = true
            durationMs = player.duration.coerceAtLeast(0)
            binding.seekbarVideo.max = durationMs.coerceAtLeast(1)
            player.isLooping = false
            binding.progressBar.visibility = View.GONE
            binding.tvVideoStatus.text = "Video ready to play"

            if (savedPositionMs > 0) {
                binding.videoView.seekTo(savedPositionMs)
                binding.seekbarVideo.progress = savedPositionMs
                showTimes(savedPositionMs, durationMs)
            } else {
                binding.seekbarVideo.progress = 0
                showTimes(0, durationMs)
            }

            if (savedIsPlaying) {
                playVideo()
            } else {
                updatePlaybackIcon()
            }
        }
        binding.videoView.setOnCompletionListener {
            handler.removeCallbacks(updateProgress)
            binding.videoView.seekTo(0)
            binding.seekbarVideo.progress = 0
            showTimes(0, durationMs)
            binding.tvVideoStatus.text = "Video playback completed"
        }
        binding.videoView.setOnErrorListener { _, what, extra ->
            prepared = false
            binding.progressBar.visibility = View.GONE
            binding.tvVideoStatus.text = "Video playback error ($what, $extra)"
            true
        }
        binding.videoView.setVideoURI(Uri.parse("android.resource://${requireContext().packageName}/$videoId"))
    }

    private fun playVideo() {
        if (!prepared) return
        if (!binding.videoView.isPlaying) {
            binding.videoView.start()
            binding.tvVideoStatus.text = "Playing video"
            updatePlaybackIcon()
            handler.removeCallbacks(updateProgress)
            handler.post(updateProgress)
        }
    }

    private fun pauseVideo() {
        if (prepared && binding.videoView.isPlaying) {
            binding.videoView.pause()
            handler.removeCallbacks(updateProgress)
            showTimes(binding.videoView.currentPosition.coerceIn(0, durationMs), durationMs)
            binding.tvVideoStatus.text = "Video paused"
            updatePlaybackIcon()
        }
    }

    private fun togglePlayback() {
        if (binding.videoView.isPlaying) pauseVideo() else playVideo()
    }

    private fun updatePlaybackIcon() {
        val playing = prepared && binding.videoView.isPlaying
        binding.btnPlayPause.setImageResource(if (playing) R.drawable.ic_pause else R.drawable.ic_play)
        binding.btnPlayPause.contentDescription = if (playing) "Pause" else "Play"
    }

    private fun showTimes(position: Int, duration: Int) {
        binding.tvElapsedTime.text = formatTime(position)
        binding.tvTotalTime.text = formatTime(duration)
    }

    private fun formatTime(ms: Int): String {
        val value = ms.coerceAtLeast(0).toLong()
        return String.format("%02d:%02d", TimeUnit.MILLISECONDS.toMinutes(value), TimeUnit.MILLISECONDS.toSeconds(value) % 60)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (_binding != null && prepared) {
            outState.putInt("saved_video_pos", binding.videoView.currentPosition)
            outState.putBoolean("saved_video_playing", binding.videoView.isPlaying)
        }
    }

    override fun onPause() {
        pauseVideo()
        super.onPause()
    }

    override fun onDestroyView() {
        handler.removeCallbacks(updateProgress)
        _binding?.videoView?.stopPlayback()
        prepared = false
        _binding = null
        super.onDestroyView()
    }
}
