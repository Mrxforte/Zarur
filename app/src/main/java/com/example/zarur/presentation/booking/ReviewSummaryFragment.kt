package com.example.zarur.presentation.booking

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
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
    private val bookingFlowViewModel: BookingFlowViewModel by activityViewModels()

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
                launch {
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
                launch {
                    bookingFlowViewModel.bookingData.collect { data ->
                        // The ids mapped to layout items inside nested cards
                        binding.root.findViewById<TextView>(R.id.tvPropertyName)?.text = data.productName
                        binding.root.findViewById<TextView>(R.id.tvPropertyLocation)?.text = data.productLocation
                        binding.root.findViewById<TextView>(R.id.tvPrice)?.text = "%,.0f".format(data.price).replace(",", " ")
                        
                        val ivProperty = binding.root.findViewById<ImageView>(R.id.ivProperty)
                        if (ivProperty != null && data.productImage.isNotBlank()) {
                            Glide.with(this@ReviewSummaryFragment)
                                .load(data.productImage)
                                .placeholder(R.drawable.onboarding3)
                                .into(ivProperty)
                        }

                        binding.tvDateRange.text = data.dates
                        binding.tvCheckInDate.text = data.checkInDate.ifBlank { "Dec 28, 2024" }
                        binding.tvCheckOutDate.text = data.checkOutDate.ifBlank { "Jan 02, 2025" }
                        
                        binding.tvAmount.text = data.formattedPrice
                        binding.tvTotal.text = data.formattedPrice
                        
                        if (data.paymentMethod.isNotBlank()) {
                            binding.tvPaymentNumber.text = data.paymentMethod
                        }
                        if (data.paymentIconRes != 0) {
                            binding.ivPayment.setImageResource(data.paymentIconRes)
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
