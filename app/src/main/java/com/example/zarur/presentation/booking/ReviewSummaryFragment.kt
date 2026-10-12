package com.example.zarur.presentation.booking

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.zarur.R
import com.example.zarur.databinding.FragmentReviewSummaryBinding
import com.example.zarur.presentation.profile.PaymentViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ReviewSummaryFragment : Fragment(R.layout.fragment_review_summary) {

    private var _binding: FragmentReviewSummaryBinding? = null
    private val binding get() = _binding!!

    private val paymentViewModel: PaymentViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentReviewSummaryBinding.bind(view)

        setupToolbar()
        setupListeners()
        observePaymentState()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupListeners() {
        binding.btnConfirm.setOnClickListener {
            if (findNavController().currentDestination?.id == R.id.reviewSummaryFragment) {
                findNavController().navigate(R.id.action_reviewSummaryFragment_to_bookingPinFragment)
            }
        }

        binding.tvChangePayment.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun observePaymentState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                paymentViewModel.uiState.collect { state ->
                    val selectedId = state.selectedMethodId
                    val selectedMethod = state.cards.find { it.id == selectedId }
                        ?: state.wallets.find { it.id == selectedId }
                        ?: state.cards.find { it.isDefault }

                    if (selectedMethod != null) {
                        binding.ivPayment.setImageResource(selectedMethod.iconResId)
                        binding.tvPaymentNumber.text = selectedMethod.formattedDisplayNumber
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
