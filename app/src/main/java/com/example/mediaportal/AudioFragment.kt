package com.example.mediaportal

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.mediaportal.databinding.FragmentAudioBinding
import java.util.concurrent.TimeUnit

class AudioFragment : Fragment() {

    private var _binding: FragmentAudioBinding? = null
    private val binding get() = _binding!!

    private var mediaPlayer: MediaPlayer? = null
    private val handler = Handler(Looper.getMainLooper())
    private var isSourceLoaded = false
    private var savedPosMs = 0
    private var savedIsPlaying = false

    // Sample fallback online stream for testing if local file is missing
    private val fallbackOnlineAudioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3"

    private val updateSeekBarRunnable = object : Runnable {
        override fun run() {
            mediaPlayer?.let { player ->
                if (player.isPlaying) {
                    val currentPos = player.currentPosition
                    binding.seekbarAudio.progress = currentPos
                    binding.tvCurrentTime.text = formatTime(currentPos.toLong())
                    handler.postDelayed(this, 500)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAudioBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        savedPosMs = savedInstanceState?.getInt("saved_audio_pos", 0) ?: 0
        savedIsPlaying = savedInstanceState?.getBoolean("saved_audio_playing", false) ?: false

        val resId = resources.getIdentifier("sample_audio", "raw", requireContext().packageName)

        if (resId != 0) {
            binding.cardAudioNotice.visibility = View.GONE
            setupLocalAudioPlayer(resId)
        } else {
            binding.cardAudioNotice.visibility = View.VISIBLE
            binding.tvAudioStatus.text = "Local sample_audio.mp3 not found in res/raw/"

            binding.btnTestOnlineAudio.setOnClickListener {
                binding.tvAudioStatus.text = "Preparing online audio sample..."
                setupOnlineAudioPlayer(fallbackOnlineAudioUrl)
            }
        }

        binding.btnPlay.setOnClickListener {
            if (!isSourceLoaded) {
                Toast.makeText(requireContext(), "No audio source ready", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            startPlayback()
        }

        binding.btnPause.setOnClickListener {
            pausePlayback()
        }

        binding.btnStop.setOnClickListener {
            restartPlayback()
        }

        binding.seekbarAudio.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    mediaPlayer?.seekTo(progress)
                    binding.tvCurrentTime.text = formatTime(progress.toLong())
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun setupLocalAudioPlayer(resId: Int) {
        releaseMediaPlayer()
        try {
            mediaPlayer = MediaPlayer.create(requireContext(), resId)
            mediaPlayer?.let { player ->
                isSourceLoaded = true
                player.setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                player.setVolume(1f, 1f)
                val duration = player.duration
                binding.seekbarAudio.max = duration
                binding.tvTotalTime.text = formatTime(duration.toLong())
                binding.tvAudioStatus.text = "Local audio ready to play"

                if (savedPosMs > 0) {
                    player.seekTo(savedPosMs)
                    binding.seekbarAudio.progress = savedPosMs
                    binding.tvCurrentTime.text = formatTime(savedPosMs.toLong())
                }

                if (savedIsPlaying) {
                    startPlayback()
                }

                player.setOnCompletionListener {
                    onPlaybackCompleted()
                }
            }
        } catch (e: Exception) {
            binding.tvAudioStatus.text = "Error loading local audio file"
        }
    }

    private fun setupOnlineAudioPlayer(url: String) {
        releaseMediaPlayer()
        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(requireContext(), Uri.parse(url))
                setOnPreparedListener { player ->
                    isSourceLoaded = true
                    val duration = player.duration
                    binding.seekbarAudio.max = duration
                    binding.tvTotalTime.text = formatTime(duration.toLong())
                    binding.tvAudioStatus.text = "Online stream ready to play"

                    if (savedPosMs > 0) {
                        player.seekTo(savedPosMs)
                        binding.seekbarAudio.progress = savedPosMs
                        binding.tvCurrentTime.text = formatTime(savedPosMs.toLong())
                    }

                    if (savedIsPlaying) {
                        startPlayback()
                    } else {
                        startPlayback()
                    }
                }
                setOnCompletionListener {
                    onPlaybackCompleted()
                }
                setOnErrorListener { _, _, _ ->
                    binding.tvAudioStatus.text = "Error loading audio stream"
                    true
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            binding.tvAudioStatus.text = "Failed to initialize audio stream"
        }
    }

    private fun startPlayback() {
        mediaPlayer?.let { player ->
            if (!player.isPlaying) {
                player.start()
                binding.tvAudioStatus.text = "Playing audio"
                handler.post(updateSeekBarRunnable)
            }
        }
    }

    private fun pausePlayback() {
        mediaPlayer?.let { player ->
            if (player.isPlaying) {
                player.pause()
                binding.tvAudioStatus.text = "Audio paused"
                handler.removeCallbacks(updateSeekBarRunnable)
            }
        }
    }

    private fun restartPlayback() {
        mediaPlayer?.let { player ->
            player.pause()
            player.seekTo(0)
            binding.seekbarAudio.progress = 0
            binding.tvCurrentTime.text = "00:00"
            binding.tvAudioStatus.text = "Audio restarted"
            handler.removeCallbacks(updateSeekBarRunnable)
            player.start()
            binding.tvAudioStatus.text = "Playing audio"
            handler.post(updateSeekBarRunnable)
        }
    }

    private fun onPlaybackCompleted() {
        binding.seekbarAudio.progress = 0
        binding.tvCurrentTime.text = "00:00"
        binding.tvAudioStatus.text = "Audio playback completed"
        handler.removeCallbacks(updateSeekBarRunnable)
    }

    private fun formatTime(millis: Long): String {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    private fun releaseMediaPlayer() {
        handler.removeCallbacks(updateSeekBarRunnable)
        mediaPlayer?.let { player ->
            if (player.isPlaying) {
                player.stop()
            }
            player.release()
        }
        mediaPlayer = null
        isSourceLoaded = false
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mediaPlayer?.let { player ->
            if (isSourceLoaded) {
                outState.putInt("saved_audio_pos", player.currentPosition)
                outState.putBoolean("saved_audio_playing", player.isPlaying)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        pausePlayback()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        releaseMediaPlayer()
        _binding = null
    }
}
