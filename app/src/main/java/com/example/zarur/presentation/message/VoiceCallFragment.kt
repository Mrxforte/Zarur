package com.example.zarur.presentation.message

import android.os.Bundle
import android.os.SystemClock
import android.view.View
import android.widget.Chronometer
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.zarur.R
import com.example.zarur.domain.model.CallStatus
import com.example.zarur.domain.model.CallType
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class VoiceCallFragment : Fragment(R.layout.fragment_voice_call) {

    private val viewModel: ChatViewModel by activityViewModels()

    private var isMuted = false
    private var isSpeakerOn = false
    private var callStartTime: Long = 0L

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        callStartTime = SystemClock.elapsedRealtime()

        val chronometer = view.findViewById<Chronometer>(R.id.tvCallStatus)
        chronometer.base = callStartTime
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
            val durationSeconds = (SystemClock.elapsedRealtime() - callStartTime) / 1000
            viewModel.recordCallLog(
                callerName = "Natasya Wilodra",
                callType = CallType.VOICE,
                callStatus = CallStatus.OUTGOING,
                durationSeconds = durationSeconds
            )
            findNavController().popBackStack()
        }
    }
}
