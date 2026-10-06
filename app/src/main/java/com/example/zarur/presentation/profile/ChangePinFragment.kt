package com.example.zarur.presentation.profile

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
import com.example.zarur.databinding.FragmentChangePinBinding
import com.example.zarur.presentation.common.PinViewModel
import com.example.zarur.util.showSuccessToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ChangePinFragment : Fragment(R.layout.fragment_change_pin) {

    private var _binding: FragmentChangePinBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PinViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentChangePinBinding.bind(view)

        setupToolbar()
        setupNumberPad()
        observeViewModel()

        binding.btnSavePin.setOnClickListener {
            viewModel.savePin()
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
                    binding.btnSavePin.isEnabled = pin.length == 4
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.successEvent.collect {
                    showSuccessToast("PIN kod muvaffaqiyatli saqlandi!")
                    findNavController().navigateUp()
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
