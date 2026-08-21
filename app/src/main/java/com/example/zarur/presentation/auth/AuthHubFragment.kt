package com.example.zarur.presentation.auth

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.zarur.R
import com.google.android.material.button.MaterialButton
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AuthHubFragment : Fragment(R.layout.fragment_auth_hub) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        view.findViewById<MaterialButton>(R.id.btnLogin).setOnClickListener {
            findNavController().navigate(R.id.action_authHubFragment_to_signInFragment)
        }

        view.findViewById<MaterialButton>(R.id.btnRegister).setOnClickListener {
            findNavController().navigate(R.id.action_authHubFragment_to_signUpFragment)
        }
    }
}
