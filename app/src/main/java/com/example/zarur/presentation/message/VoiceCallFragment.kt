package com.example.zarur.presentation.message

import android.os.Bundle
import android.os.SystemClock
import android.view.View
import android.widget.Chronometer
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.zarur.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class VoiceCallFragment : Fragment(R.layout.fragment_voice_call) {

    private var isMuted = false
    private var isSpeakerOn = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val chronometer = view.findViewById<Chronometer>(R.id.tvCallStatus)
        chronometer.base = SystemClock.elapsedRealtime()
        chronometer.start()

        val btnMute = view.findViewById<ImageView>(R.id.btnMute)
        btnMute.setOnClickListener {
            isMuted = !isMuted
            btnMute.alpha = if (isMuted) 0.5f else 1.0f
        }

        val btnSpeaker = view.findViewById<ImageView>(R.id.btnSpeaker)
        btnSpeaker.setOnClickListener {
            isSpeakerOn = !isSpeakerOn
            btnSpeaker.alpha = if (isSpeakerOn) 1.0f else 0.5f
        }

        view.findViewById<View>(R.id.btnEndCall).setOnClickListener {
            chronometer.stop()
            findNavController().popBackStack()
        }
    }
}
