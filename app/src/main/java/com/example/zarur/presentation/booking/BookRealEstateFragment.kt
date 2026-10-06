package com.example.zarur.presentation.booking

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.zarur.R
import com.example.zarur.databinding.FragmentBookRealEstateBinding
import dagger.hilt.android.AndroidEntryPoint

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@AndroidEntryPoint
class BookRealEstateFragment : Fragment(R.layout.fragment_book_real_estate) {

    private var _binding: FragmentBookRealEstateBinding? = null
    private val binding get() = _binding!!

    private val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentBookRealEstateBinding.bind(view)

        setupToolbar()
        setupCalendar()
        setupContinueButton()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupCalendar() {
        val calendar = Calendar.getInstance()
        
        // Set default dates
        binding.tvCheckInDate.text = dateFormat.format(calendar.time)
        calendar.add(Calendar.DAY_OF_YEAR, 4)
        binding.tvCheckOutDate.text = dateFormat.format(calendar.time)

        // Don't allow selecting dates in the past
        binding.calendarView.minDate = System.currentTimeMillis() - 1000

        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val selectedDate = Calendar.getInstance()
            selectedDate.set(year, month, dayOfMonth)
            binding.tvCheckInDate.text = dateFormat.format(selectedDate.time)
            
            // Set checkout date to 4 days later by default
            selectedDate.add(Calendar.DAY_OF_YEAR, 4)
            binding.tvCheckOutDate.text = dateFormat.format(selectedDate.time)
        }
    }

    private fun setupContinueButton() {
        binding.btnContinue.setOnClickListener {
            if (findNavController().currentDestination?.id == R.id.bookRealEstateFragment) {
                findNavController().navigate(R.id.action_bookRealEstateFragment_to_bookingInfoFragment)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
