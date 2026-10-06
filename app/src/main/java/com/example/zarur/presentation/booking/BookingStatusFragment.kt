package com.example.zarur.presentation.booking

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.zarur.R
import com.example.zarur.databinding.FragmentBookingStatusBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BookingStatusFragment : Fragment(R.layout.fragment_booking_status) {

    private var _binding: FragmentBookingStatusBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentBookingStatusBinding.bind(view)

        binding.btnViewReceipt.setOnClickListener {
            if (findNavController().currentDestination?.id == R.id.bookingStatusFragment) {
                findNavController().navigate(R.id.action_bookingStatusFragment_to_EReceiptFragment)
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
