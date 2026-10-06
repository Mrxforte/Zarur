package com.example.zarur.presentation.message

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.zarur.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class VideoCallFragment : Fragment(R.layout.fragment_video_call) {

    private var isMuted = false
    private var isCameraOff = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnMuteVideo = view.findViewById<ImageView>(R.id.btnMuteVideo)
        btnMuteVideo.setOnClickListener {
            isMuted = !isMuted
            btnMuteVideo.alpha = if (isMuted) 0.5f else 1.0f
        }

        val btnToggleCamera = view.findViewById<ImageView>(R.id.btnToggleCamera)
        btnToggleCamera.setOnClickListener {
            isCameraOff = !isCameraOff
            btnToggleCamera.alpha = if (isCameraOff) 0.5f else 1.0f
        }

        view.findViewById<View>(R.id.btnEndVideoCall).setOnClickListener {
            findNavController().popBackStack()
        }
    }
}
