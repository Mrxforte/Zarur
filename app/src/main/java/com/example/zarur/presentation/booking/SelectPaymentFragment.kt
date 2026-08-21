package com.example.zarur.presentation.booking

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.zarur.R
import com.example.zarur.databinding.FragmentSelectPaymentBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SelectPaymentFragment : Fragment(R.layout.fragment_select_payment) {

    private var _binding: FragmentSelectPaymentBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSelectPaymentBinding.bind(view)

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnContinue.setOnClickListener {
            findNavController().navigate(R.id.action_selectPaymentFragment_to_reviewSummaryFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
