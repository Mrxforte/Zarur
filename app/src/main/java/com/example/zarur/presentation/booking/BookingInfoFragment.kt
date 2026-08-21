package com.example.zarur.presentation.booking

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.zarur.R
import com.example.zarur.databinding.FragmentBookingInfoBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BookingInfoFragment : Fragment(R.layout.fragment_booking_info) {

    private var _binding: FragmentBookingInfoBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentBookingInfoBinding.bind(view)

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnContinue.setOnClickListener {
            findNavController().navigate(R.id.action_bookingInfoFragment_to_selectPaymentFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
