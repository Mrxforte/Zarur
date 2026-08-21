package com.example.zarur.presentation.message

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.zarur.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class VoiceCallFragment : Fragment(R.layout.fragment_voice_call) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<View>(R.id.btnEndCall).setOnClickListener {
            findNavController().popBackStack()
        }
    }
}
