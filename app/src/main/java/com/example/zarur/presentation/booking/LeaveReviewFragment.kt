package com.example.zarur.presentation.booking

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.zarur.R
import com.example.zarur.databinding.FragmentLeaveReviewBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LeaveReviewFragment : Fragment(R.layout.fragment_leave_review) {

    private var _binding: FragmentLeaveReviewBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentLeaveReviewBinding.bind(view)

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnSubmit.setOnClickListener {
            findNavController().navigate(R.id.action_leaveReviewFragment_to_exploreFragment)
        }

        binding.btnMaybeLater.setOnClickListener {
            findNavController().navigate(R.id.action_leaveReviewFragment_to_exploreFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
