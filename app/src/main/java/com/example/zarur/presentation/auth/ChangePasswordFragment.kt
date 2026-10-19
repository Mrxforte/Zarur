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
import com.example.zarur.databinding.FragmentChangePasswordBinding
import com.example.zarur.presentation.common.UiState
import com.example.zarur.util.showErrorToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ChangePasswordFragment : Fragment(R.layout.fragment_change_password) {

    private var _binding: FragmentChangePasswordBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AuthViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentChangePasswordBinding.bind(view)

        setupToolbar()
        setupListeners()
        observeViewModel()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupListeners() {
        binding.btnContinue.setOnClickListener {
            val newPassword = binding.tilNewPassword.editText?.text?.toString()?.trim().orEmpty()
            val confirmPassword = binding.tilConfirmNewPassword.editText?.text?.toString()?.trim().orEmpty()

            binding.tilNewPassword.error = null
            binding.tilConfirmNewPassword.error = null

            if (newPassword.length < 6) {
                binding.tilNewPassword.error = getString(R.string.error_weak_password)
                return@setOnClickListener
            }

            if (newPassword != confirmPassword) {
                binding.tilConfirmNewPassword.error = getString(R.string.error_passwords_do_not_match)
                return@setOnClickListener
            }

            viewModel.updatePassword(newPassword)
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.updatePasswordState.collect { state ->
                    when (state) {
                        is UiState.Loading -> {
                            binding.btnContinue.isEnabled = false
                            binding.btnContinue.text = getString(R.string.submitting)
                        }
                        is UiState.Success -> {
                            binding.btnContinue.isEnabled = true
                            binding.btnContinue.text = getString(R.string.button_continue)
                            viewModel.resetUpdatePasswordState()
                            ResetPasswordSuccessDialog().show(parentFragmentManager, "success")
                        }
                        is UiState.Error -> {
                            binding.btnContinue.isEnabled = true
                            binding.btnContinue.text = getString(R.string.button_continue)
                            showErrorToast(state.message)
                        }
                        null -> {
                            binding.btnContinue.isEnabled = true
                            binding.btnContinue.text = getString(R.string.button_continue)
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
