package com.example.zarur.presentation.explore

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.zarur.R
import com.example.zarur.databinding.FragmentExploreBinding
import com.example.zarur.presentation.common.PropertyAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ExploreFragment : Fragment(R.layout.fragment_explore) {

    private var _binding: FragmentExploreBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentExploreBinding.bind(view)

        setupRecyclerViews()

        binding.ivNotification.setOnClickListener {
            findNavController().navigate(R.id.action_exploreFragment_to_notificationFragment)
        }

        binding.ivFilter.setOnClickListener {
            FilterBottomSheet().show(parentFragmentManager, "filter")
        }

        binding.tvSeeAllRecommendation.setOnClickListener {
            findNavController().navigate(R.id.action_exploreFragment_to_recommendationFragment)
        }
    }

    private fun setupRecyclerViews() {
        val adapter = PropertyAdapter {
            findNavController().navigate(R.id.action_exploreFragment_to_propertyDetailFragment)
        }

        binding.rvFeatured.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.rvFeatured.adapter = adapter

        binding.rvRecommendation.layoutManager = LinearLayoutManager(context)
        binding.rvRecommendation.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
