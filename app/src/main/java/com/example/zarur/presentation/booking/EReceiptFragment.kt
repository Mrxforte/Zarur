package com.example.zarur.presentation.booking

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.zarur.R
import com.example.zarur.databinding.FragmentEReceiptBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EReceiptFragment : Fragment(R.layout.fragment_e_receipt) {

    private var _binding: FragmentEReceiptBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentEReceiptBinding.bind(view)

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnReview.setOnClickListener {
            if (findNavController().currentDestination?.id == R.id.EReceiptFragment) {
                findNavController().navigate(R.id.action_EReceiptFragment_to_leaveReviewFragment)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
