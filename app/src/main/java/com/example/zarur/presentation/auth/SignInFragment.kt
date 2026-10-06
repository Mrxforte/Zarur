package com.example.zarur.presentation.auth

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.zarur.R
import com.example.zarur.databinding.FragmentSignInBinding
import com.example.zarur.presentation.common.UiState
import com.example.zarur.util.showErrorToast
import com.example.zarur.util.showInfoToast
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SignInFragment : Fragment(R.layout.fragment_sign_in) {

    private var _binding: FragmentSignInBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AuthViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSignInBinding.bind(view)

        setupListeners()
        observeViewModel()
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.tvSignUp.setOnClickListener {
            findNavController().navigate(R.id.action_signInFragment_to_signUpFragment)
        }

        binding.tvForgotPassword.setOnClickListener {
            findNavController().navigate(R.id.action_signInFragment_to_forgotPasswordFragment)
        }

        binding.btnSignIn.setOnClickListener {
            val email = binding.tilGmail.editText?.text?.toString()?.trim() ?: ""
            val password = binding.tilPassword.editText?.text?.toString()?.trim() ?: ""

            if (email.isEmpty()) {
                binding.tilGmail.error = getString(R.string.label_gmail)
                return@setOnClickListener
            } else {
                binding.tilGmail.error = null
            }

            if (password.isEmpty()) {
                binding.tilPassword.error = getString(R.string.label_password)
                return@setOnClickListener
            } else {
                binding.tilPassword.error = null
            }

            viewModel.signIn(email, password)
        }

        binding.ivGoogle.setOnClickListener {
            signInWithGoogle()
        }
    }

    private fun signInWithGoogle() {
        viewLifecycleOwner.lifecycleScope.launch {
            val credentialManager = CredentialManager.create(requireContext())
            
            // Note: Update R.string.default_web_client_id in strings.xml with your actual Firebase Web Client ID
            val webClientId = getString(R.string.default_web_client_id)
            if (webClientId == "YOUR_WEB_CLIENT_ID") {
                showInfoToast("Google Sign-In requires a valid Web Client ID in strings.xml")
                return@launch
            }

            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(webClientId)
                .setAutoSelectEnabled(true)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            try {
                val result = credentialManager.getCredential(
                    request = request,
                    context = requireActivity()
                )
                handleGoogleSignInResult(result)
            } catch (e: GetCredentialException) {
                showErrorToast("Google Sign-in failed: ${e.message}")
            }
        }
    }

    private fun handleGoogleSignInResult(result: GetCredentialResponse) {
        val credential = result.credential
        if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            try {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken
                viewModel.signInWithGoogle(idToken)
            } catch (_: Exception) {
                showErrorToast("Failed to extract Google credentials")
            }
        } else {
            showErrorToast("Unexpected credential type")
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.signInState.collect { state ->
                    when (state) {
                        is UiState.Loading -> {
                            binding.btnSignIn.isEnabled = false
                            binding.btnSignIn.text = getString(R.string.button_sign_in_caps)
                        }
                        is UiState.Success -> {
                            binding.btnSignIn.isEnabled = true
                            binding.btnSignIn.text = getString(R.string.button_sign_in_caps)
                            viewModel.resetSignInState()
                            findNavController().navigate(R.id.action_signInFragment_to_enableLocationFragment)
                        }
                        is UiState.Error -> {
                            binding.btnSignIn.isEnabled = true
                            binding.btnSignIn.text = getString(R.string.button_sign_in_caps)
                            showErrorToast(state.message)
                        }
                        null -> {
                            binding.btnSignIn.isEnabled = true
                            binding.btnSignIn.text = getString(R.string.button_sign_in_caps)
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
