package com.example.zarur.presentation.booking

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.zarur.R
import com.example.zarur.databinding.FragmentBookingPinBinding
import com.example.zarur.presentation.common.PinViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BookingPinFragment : Fragment(R.layout.fragment_booking_pin) {

    private var _binding: FragmentBookingPinBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PinViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentBookingPinBinding.bind(view)

        setupToolbar()
        setupNumberPad()
        observeViewModel()

        binding.btnContinue.setOnClickListener {
            viewModel.validatePin()
        }
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupNumberPad() {
        binding.glNumberPad.children.forEach { view ->
            view.setOnClickListener {
                when (view) {
                    is TextView -> {
                        val text = view.text.toString()
                        if (text != "*") {
                            viewModel.onNumberClick(text)
                        }
                    }
                    else -> {
                        // Assuming the delete button is an ImageView
                        viewModel.onDeleteClick()
                    }
                }
            }
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.pinState.collect { pin ->
                    updatePinIndicators(pin.length)
                    binding.btnContinue.isEnabled = pin.length == 4
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.successEvent.collect {
                    if (findNavController().currentDestination?.id == R.id.bookingPinFragment) {
                        findNavController().navigate(R.id.action_bookingPinFragment_to_bookingStatusFragment)
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.errorEvent.collect {
                    // Show error e.g. Toast or shake animation
                }
            }
        }
    }

    private fun updatePinIndicators(length: Int) {
        binding.llPinContainer.children.forEachIndexed { index, view ->
            view.isEnabled = index < length
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
