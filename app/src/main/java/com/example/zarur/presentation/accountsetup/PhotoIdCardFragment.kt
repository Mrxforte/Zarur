package com.example.zarur.presentation.accountsetup

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.zarur.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PhotoIdCardFragment : Fragment(R.layout.fragment_photo_id_card) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<View>(R.id.toolbar).setOnClickListener {
            findNavController().navigateUp()
        }
        view.findViewById<View>(R.id.fabCapture).setOnClickListener {
            findNavController().navigate(R.id.action_photoIdCardFragment_to_selfieWithIdFragment)
        }
    }
}
