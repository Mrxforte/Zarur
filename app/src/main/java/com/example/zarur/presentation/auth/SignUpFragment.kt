package com.example.zarur.presentation.auth

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.zarur.R
import com.example.zarur.databinding.FragmentSignUpBinding
import com.example.zarur.presentation.common.UiState
import com.example.zarur.util.showErrorToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SignUpFragment : Fragment(R.layout.fragment_sign_up) {

    private var _binding: FragmentSignUpBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AuthViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSignUpBinding.bind(view)

        setupListeners()
        observeViewModel()
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.tvSignIn.setOnClickListener {
            findNavController().navigate(R.id.action_signUpFragment_to_signInFragment)
        }

        binding.btnSignUp.setOnClickListener {
            val fullName = binding.tilFullName.editText?.text?.toString()?.trim() ?: ""
            val email = binding.tilPhoneGmail.editText?.text?.toString()?.trim() ?: ""
            val password = binding.tilPassword.editText?.text?.toString()?.trim() ?: ""
            val confirmPassword = binding.tilConfirmPassword.editText?.text?.toString()?.trim() ?: ""

            if (fullName.isEmpty()) {
                binding.tilFullName.error = getString(R.string.label_full_name)
                return@setOnClickListener
            } else {
                binding.tilFullName.error = null
            }

            if (email.isEmpty()) {
                binding.tilPhoneGmail.error = getString(R.string.label_phone_gmail)
                return@setOnClickListener
            } else {
                binding.tilPhoneGmail.error = null
            }

            if (password.length < 6) {
                binding.tilPassword.error = getString(R.string.label_password)
                return@setOnClickListener
            } else {
                binding.tilPassword.error = null
            }

            if (password != confirmPassword) {
                binding.tilConfirmPassword.error = getString(R.string.label_confirm_password)
                return@setOnClickListener
            } else {
                binding.tilConfirmPassword.error = null
            }

            viewModel.signUp(email, password, fullName)
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.signUpState.collect { state ->
                    when (state) {
                        is UiState.Loading -> {
                            binding.btnSignUp.isEnabled = false
                            binding.btnSignUp.text = getString(R.string.button_sign_up_caps)
                        }
                        is UiState.Success -> {
                            binding.btnSignUp.isEnabled = true
                            binding.btnSignUp.text = getString(R.string.button_sign_up_caps)
                            viewModel.resetSignUpState()
                            findNavController().navigate(R.id.action_signUpFragment_to_exploreFragment)
                        }
                        is UiState.Error -> {
                            binding.btnSignUp.isEnabled = true
                            binding.btnSignUp.text = getString(R.string.button_sign_up_caps)
                            showErrorToast(state.message)
                        }
                        null -> {
                            binding.btnSignUp.isEnabled = true
                            binding.btnSignUp.text = getString(R.string.button_sign_up_caps)
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
