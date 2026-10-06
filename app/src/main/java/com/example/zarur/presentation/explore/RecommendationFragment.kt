package com.example.zarur.presentation.explore

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.zarur.R
import com.example.zarur.databinding.FragmentRecommendationBinding
import com.example.zarur.presentation.common.ProductAdapter
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
        showShimmerThenContent()
    }

    private fun showShimmerThenContent() {
        binding.shimmerRecommendation.root.isVisible = true
        binding.rvRecommendation.isVisible = false
        binding.shimmerRecommendation.shimmerViewContainer.startShimmer()
        binding.rvRecommendation.postDelayed({
            if (_binding != null) {
                binding.shimmerRecommendation.shimmerViewContainer.stopShimmer()
                binding.shimmerRecommendation.root.isVisible = false
                binding.rvRecommendation.isVisible = true
            }
        }, 1000)
    }

    private fun setupRecyclerView() {
        binding.rvRecommendation.layoutManager = GridLayoutManager(context, 2)
        binding.rvRecommendation.adapter = ProductAdapter(
            onItemClick = { product ->
                val bundle = Bundle().apply {
                    putString("productId", product.id)
                }
                findNavController().navigate(R.id.productDetailFragment, bundle)
            }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
