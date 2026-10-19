package com.example.zarur.presentation.profile

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.zarur.R
import com.example.zarur.data.local.AppPreferences
import com.example.zarur.databinding.FragmentSecurityBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SecurityFragment : Fragment(R.layout.fragment_security) {

    private var _binding: FragmentSecurityBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var appPreferences: AppPreferences

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSecurityBinding.bind(view)

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.switchRememberMe.isChecked = appPreferences.isLoggedIn
        binding.switchRememberMe.setOnCheckedChangeListener { _, isChecked ->
            appPreferences.isLoggedIn = isChecked
        }

        binding.btnChangePassword.setOnClickListener {
            findNavController().navigate(R.id.action_securityFragment_to_changePasswordFragment)
        }

        binding.btnChangePin.setOnClickListener {
            findNavController().navigate(R.id.action_securityFragment_to_changePinFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
