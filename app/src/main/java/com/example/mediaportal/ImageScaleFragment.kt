package com.example.mediaportal

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.mediaportal.databinding.FragmentImageScaleBinding

class ImageScaleFragment : Fragment() {

    private var _binding: FragmentImageScaleBinding? = null
    private val binding get() = _binding!!

    // High quality internet image URL provided for pinch-to-zoom
    private val sampleImageUrl = "https://i.pinimg.com/originals/cd/ef/1d/cdef1d55324240ef33d788d54ed1e896.jpg"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentImageScaleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadImage()

        binding.btnRetry.setOnClickListener {
            loadImage()
        }

        binding.btnResetZoom.setOnClickListener {
            binding.zoomableImageView.resetZoom()
        }
    }

    private fun loadImage() {
        binding.progressBar.visibility = View.VISIBLE
        binding.layoutError.visibility = View.GONE
        binding.zoomableImageView.visibility = View.VISIBLE

        Glide.with(this)
            .load(sampleImageUrl)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>,
                    isFirstResource: Boolean
                ): Boolean {
                    binding.progressBar.visibility = View.GONE
                    binding.layoutError.visibility = View.VISIBLE
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable,
                    model: Any,
                    target: Target<Drawable>?,
                    dataSource: DataSource,
                    isFirstResource: Boolean
                ): Boolean {
                    binding.progressBar.visibility = View.GONE
                    binding.layoutError.visibility = View.GONE
                    binding.zoomableImageView.post {
                        binding.zoomableImageView.resetZoom()
                    }
                    return false
                }
            })
            .into(binding.zoomableImageView)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
