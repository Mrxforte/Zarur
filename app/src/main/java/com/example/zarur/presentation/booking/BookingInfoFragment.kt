package com.example.zarur.presentation.booking

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.zarur.R
import com.example.zarur.databinding.FragmentBookingInfoBinding
import com.example.zarur.domain.usecase.IsLoggedInUseCase
import com.example.zarur.util.showErrorToast
import com.example.zarur.util.showInfoToast
import com.google.android.material.datepicker.MaterialDatePicker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject
import androidx.fragment.app.activityViewModels

import com.example.zarur.util.ensureAuth
import com.example.zarur.util.showAuthRequiredDialog

@AndroidEntryPoint
class BookingInfoFragment : Fragment(R.layout.fragment_booking_info) {

    private var _binding: FragmentBookingInfoBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var isLoggedInUseCase: IsLoggedInUseCase

    private val viewModel: BookingInfoViewModel by viewModels()
    private val bookingFlowViewModel: BookingFlowViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentBookingInfoBinding.bind(view)

        if (!isLoggedInUseCase()) {
            showAuthRequiredDialog()
        }

        setupToolbar()
        setupDatePicker()
        setupContinueButton()
        observeViewModel()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupDatePicker() {
        val openDatePicker = {
            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(getString(R.string.label_dob))
                .build()

            datePicker.addOnPositiveButtonClickListener { selection ->
                val formatter = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }
                binding.etDob.setText(formatter.format(Date(selection)))
            }

            datePicker.show(childFragmentManager, "DOB_DATE_PICKER")
        }

        binding.etDob.setOnClickListener { openDatePicker() }
        binding.tilDob.setEndIconOnClickListener { openDatePicker() }
    }

    private fun setupContinueButton() {
        binding.btnContinue.setOnClickListener {
            ensureAuth(isLoggedInUseCase()) {
                val fullName = binding.etFullName.text?.toString().orEmpty().trim()
                val nickname = binding.etNickname.text?.toString().orEmpty().trim()
                val dob = binding.etDob.text?.toString().orEmpty().trim()
                val email = binding.etEmail.text?.toString().orEmpty().trim()
                val phone = binding.etPhone.text?.toString().orEmpty().trim()

                bookingFlowViewModel.setContactInfo(fullName, nickname, email, phone, dob)

                viewModel.validateAndSubmit(
                    fullName = fullName,
                    nickname = nickname,
                    dob = dob,
                    email = email,
                    phone = phone
                )
            }
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.userProfile.collect { user ->
                        if (user != null) {
                            if (binding.etFullName.text.isNullOrEmpty() && user.fullName.isNotBlank()) {
                                binding.etFullName.setText(user.fullName)
                            }
                            if (binding.etNickname.text.isNullOrEmpty() && user.nickname.isNotBlank()) {
                                binding.etNickname.setText(user.nickname)
                            }
                            if (binding.etEmail.text.isNullOrEmpty() && user.email.isNotBlank()) {
                                binding.etEmail.setText(user.email)
                            }
                            if (binding.etPhone.text.isNullOrEmpty() && user.phone.isNotBlank()) {
                                binding.etPhone.setText(user.phone)
                            }
                            if (binding.etDob.text.isNullOrEmpty() && user.dob.isNotBlank()) {
                                binding.etDob.setText(user.dob)
                            }
                        }
                    }
                }

                launch {
                    viewModel.uiEvent.collect { event ->
                        when (event) {
                            is BookingInfoUiEvent.NavigateToPayment -> {
                                if (findNavController().currentDestination?.id == R.id.bookingInfoFragment) {
                                    findNavController().navigate(R.id.action_bookingInfoFragment_to_selectPaymentFragment)
                                }
                            }
                            is BookingInfoUiEvent.ShowError -> {
                                showErrorToast(event.message)
                            }
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
