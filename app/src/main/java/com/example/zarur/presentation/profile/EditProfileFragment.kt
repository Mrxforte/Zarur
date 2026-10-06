package com.example.zarur.presentation.profile

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.zarur.R
import com.example.zarur.databinding.FragmentEditProfileBinding
import com.example.zarur.presentation.common.UiState
import com.example.zarur.util.showErrorToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class EditProfileFragment : Fragment(R.layout.fragment_edit_profile) {

    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProfileViewModel by activityViewModels()

    private val imagePicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            Glide.with(this).load(it).into(binding.ivAvatar)
            viewModel.setSelectedAvatar(it.toString())
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentEditProfileBinding.bind(view)

        setupGenderDropdown()
        setupListeners()
        observeViewModel()
    }

    private fun setupGenderDropdown() {
        val genders = arrayOf("Male", "Female", "Other")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, genders)
        binding.etGender.setAdapter(adapter)
    }

    private fun setupListeners() {
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
        binding.ivBack.setOnClickListener { findNavController().navigateUp() }

        binding.btnEditAvatar.setOnClickListener {
            imagePicker.launch("image/*")
        }

        binding.btnUpdate.setOnClickListener {
            val fullName = binding.etFullName.text?.toString()?.trim() ?: ""
            val nickname = binding.etNickname.text?.toString()?.trim() ?: ""
            val email = binding.etEmail.text?.toString()?.trim() ?: ""
            val phone = binding.etPhone.text?.toString()?.trim() ?: ""
            val dob = binding.etDob.text?.toString()?.trim() ?: ""
            val gender = binding.etGender.text?.toString()?.trim() ?: ""

            viewModel.updateProfile(
                fullName = fullName,
                nickname = nickname,
                email = email,
                phone = phone,
                gender = gender,
                dob = dob
            )
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.profileState.collect { uiState ->
                    if (uiState is UiState.Success) {
                        val user = uiState.data
                        if (binding.etFullName.text.isNullOrEmpty()) {
                            binding.etFullName.setText(user.fullName)
                        }
                        if (binding.etNickname.text.isNullOrEmpty()) {
                            binding.etNickname.setText(user.nickname)
                        }
                        if (binding.etEmail.text.isNullOrEmpty()) {
                            binding.etEmail.setText(user.email)
                        }
                        if (binding.etPhone.text.isNullOrEmpty()) {
                            binding.etPhone.setText(user.phone)
                        }
                        if (binding.etDob.text.isNullOrEmpty()) {
                            binding.etDob.setText(user.dob)
                        }
                        if (binding.etGender.text.isNullOrEmpty() && user.gender.isNotEmpty()) {
                            binding.etGender.setText(user.gender, false)
                        }
                        if (!user.avatarUrl.isNullOrEmpty()) {
                            Glide.with(requireContext())
                                .load(user.avatarUrl)
                                .placeholder(R.drawable.onboarding1)
                                .into(binding.ivAvatar)
                        }
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.updateEvent.collect { event ->
                    when (event) {
                        is UiState.Loading -> {
                            binding.btnUpdate.isEnabled = false
                            binding.btnUpdate.text = getString(R.string.button_update)
                        }
                        is UiState.Success -> {
                            binding.btnUpdate.isEnabled = true
                            binding.btnUpdate.text = getString(R.string.button_update)
                            findNavController().navigateUp()
                        }
                        is UiState.Error -> {
                            binding.btnUpdate.isEnabled = true
                            binding.btnUpdate.text = getString(R.string.button_update)
                            showErrorToast(event.message)
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
