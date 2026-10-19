package com.example.zarur.presentation.booking

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.zarur.R
import com.example.zarur.databinding.FragmentBookingStatusBinding
import com.example.zarur.domain.model.BookingItem
import com.example.zarur.domain.model.BookingStatus
import com.example.zarur.domain.repository.BookingRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.fragment.app.activityViewModels

@AndroidEntryPoint
class BookingStatusFragment : Fragment(R.layout.fragment_booking_status) {

    private var _binding: FragmentBookingStatusBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var bookingRepository: BookingRepository
    
    private val bookingFlowViewModel: BookingFlowViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentBookingStatusBinding.bind(view)

        lifecycleScope.launch {
            bookingFlowViewModel.finalizeBookingAndCreateReceipt()
        }

        binding.btnViewReceipt.setOnClickListener {
            if (findNavController().currentDestination?.id == R.id.bookingStatusFragment) {
                val receipt = bookingFlowViewModel.latestReceipt.value
                val bundle = Bundle().apply {
                    if (receipt != null) {
                        putSerializable("receipt", receipt)
                    }
                }
                findNavController().navigate(R.id.action_bookingStatusFragment_to_EReceiptFragment, bundle)
            }
        }

        binding.btnCancel.setOnClickListener {
            if (findNavController().currentDestination?.id == R.id.bookingStatusFragment) {
                findNavController().navigate(R.id.action_bookingStatusFragment_to_exploreFragment)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
