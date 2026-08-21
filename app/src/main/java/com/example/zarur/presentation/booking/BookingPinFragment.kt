package com.example.zarur.presentation.booking

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.zarur.R
import com.example.zarur.databinding.FragmentBookingPinBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BookingPinFragment : Fragment(R.layout.fragment_booking_pin) {

    private var _binding: FragmentBookingPinBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentBookingPinBinding.bind(view)

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnContinue.setOnClickListener {
            findNavController().navigate(R.id.action_bookingPinFragment_to_bookingStatusFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
