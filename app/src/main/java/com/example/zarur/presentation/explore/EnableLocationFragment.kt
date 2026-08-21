package com.example.zarur.presentation.explore

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.zarur.R
import com.google.android.material.button.MaterialButton
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EnableLocationFragment : Fragment(R.layout.fragment_enable_location) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        view.findViewById<MaterialButton>(R.id.btnEnableLocation).setOnClickListener {
            findNavController().navigate(R.id.action_enableLocationFragment_to_exploreFragment)
        }
        
        view.findViewById<MaterialButton>(R.id.btnCancel).setOnClickListener {
            findNavController().popBackStack()
        }
    }
}
