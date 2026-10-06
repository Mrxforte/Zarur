package com.example.zarur.presentation.booking

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.zarur.R
import com.example.zarur.databinding.FragmentReviewSummaryBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ReviewSummaryFragment : Fragment(R.layout.fragment_review_summary) {

    private var _binding: FragmentReviewSummaryBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentReviewSummaryBinding.bind(view)

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnConfirm.setOnClickListener {
            if (findNavController().currentDestination?.id == R.id.reviewSummaryFragment) {
                findNavController().navigate(R.id.action_reviewSummaryFragment_to_bookingPinFragment)
            }
        }

        binding.tvChangePayment.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
