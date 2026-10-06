package com.example.zarur.presentation.accountsetup

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
import com.example.zarur.databinding.FragmentFillProfileBinding
import com.example.zarur.presentation.common.UiState
import com.example.zarur.presentation.profile.ProfileViewModel
import com.example.zarur.util.showErrorToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FillProfileFragment : Fragment(R.layout.fragment_fill_profile) {

    private var _binding: FragmentFillProfileBinding? = null
    private val binding get() = _binding!!

    private val profileViewModel: ProfileViewModel by activityViewModels()

    private val imagePicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            Glide.with(this).load(it).into(binding.ivAvatar)
            profileViewModel.setSelectedAvatar(it.toString())
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentFillProfileBinding.bind(view)

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
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnEditAvatar.setOnClickListener {
            imagePicker.launch("image/*")
        }

        binding.btnContinue.setOnClickListener {
            val fullName = binding.etFullName.text?.toString()?.trim() ?: ""
            val nickname = binding.etNickname.text?.toString()?.trim() ?: ""
            val email = binding.etEmail.text?.toString()?.trim() ?: ""
            val phone = binding.etPhone.text?.toString()?.trim() ?: ""
            val dob = binding.etDob.text?.toString()?.trim() ?: ""
            val gender = binding.etGender.text?.toString()?.trim() ?: ""

            profileViewModel.updateProfile(
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
                profileViewModel.updateEvent.collect { state ->
                    when (state) {
                        is UiState.Loading -> {
                            binding.btnContinue.isEnabled = false
                            binding.btnContinue.text = getString(R.string.button_continue)
                        }
                        is UiState.Success -> {
                            binding.btnContinue.isEnabled = true
                            binding.btnContinue.text = getString(R.string.button_continue)
                            findNavController().navigate(R.id.action_fillProfileFragment_to_exploreFragment)
                        }
                        is UiState.Error -> {
                            binding.btnContinue.isEnabled = true
                            binding.btnContinue.text = getString(R.string.button_continue)
                            showErrorToast(state.message)
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
