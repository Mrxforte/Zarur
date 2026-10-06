package com.example.zarur.presentation.profile

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.zarur.R
import dagger.hilt.android.AndroidEntryPoint

import com.example.zarur.databinding.FragmentMyBookingsBinding

@AndroidEntryPoint
class MyBookingsFragment : Fragment(R.layout.fragment_my_bookings) {

    private var _binding: FragmentMyBookingsBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentMyBookingsBinding.bind(view)

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
