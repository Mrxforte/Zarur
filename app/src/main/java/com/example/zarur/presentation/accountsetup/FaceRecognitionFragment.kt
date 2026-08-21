package com.example.zarur.presentation.accountsetup

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.zarur.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FaceRecognitionFragment : Fragment(R.layout.fragment_face_recognition) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<View>(R.id.toolbar).setOnClickListener {
            findNavController().navigateUp()
        }
        view.findViewById<View>(R.id.btnContinue).setOnClickListener {
            findNavController().navigate(R.id.action_faceRecognitionFragment_to_faceRecognitionScanFragment)
        }
        view.findViewById<View>(R.id.btnSkip).setOnClickListener {
            findNavController().navigate(R.id.action_faceRecognitionFragment_to_faceRecognitionScanFragment)
        }
    }
}
