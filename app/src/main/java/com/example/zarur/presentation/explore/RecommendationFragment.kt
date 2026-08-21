package com.example.zarur.presentation.explore

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.zarur.R
import com.example.zarur.databinding.FragmentRecommendationBinding
import com.example.zarur.presentation.common.PropertyAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RecommendationFragment : Fragment(R.layout.fragment_recommendation) {

    private var _binding: FragmentRecommendationBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentRecommendationBinding.bind(view)

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        binding.rvRecommendation.layoutManager = LinearLayoutManager(context)
        binding.rvRecommendation.adapter = PropertyAdapter {
            findNavController().navigate(R.id.action_recommendationFragment_to_propertyDetailFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
