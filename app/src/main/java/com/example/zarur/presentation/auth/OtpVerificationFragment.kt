package com.example.zarur.presentation.auth

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.zarur.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OtpVerificationFragment : Fragment(R.layout.fragment_otp_verification) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<View>(R.id.toolbar).setOnClickListener {
            findNavController().navigateUp()
        }
        view.findViewById<View>(R.id.btnVerify).setOnClickListener {
            findNavController().navigate(R.id.action_otpVerificationFragment_to_changePasswordFragment)
        }
    }
}
