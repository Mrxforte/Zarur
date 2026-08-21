package com.example.zarur.presentation.intro

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.zarur.R
import com.example.zarur.data.local.AppPreferences
import com.google.android.material.button.MaterialButton
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class WelcomeIntroFragment : Fragment(R.layout.fragment_welcome_intro) {

    @Inject
    lateinit var appPreferences: AppPreferences

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!appPreferences.isFirstTimeLaunch) {
            findNavController().navigate(R.id.action_welcomeIntroFragment_to_authHubFragment)
            return
        }

        view.findViewById<MaterialButton>(R.id.btnGetStarted).setOnClickListener {
            findNavController().navigate(R.id.action_welcomeIntroFragment_to_onboardingFragment)
        }
    }
}
