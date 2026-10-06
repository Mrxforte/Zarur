package com.example.zarur.presentation.explore

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.zarur.R
import com.example.zarur.databinding.FragmentSearchResultsBinding
import com.example.zarur.presentation.common.ProductAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SearchResultsFragment : Fragment(R.layout.fragment_search_results) {

    private var _binding: FragmentSearchResultsBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSearchResultsBinding.bind(view)

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.ivFilter.setOnClickListener {
            FilterBottomSheet().show(parentFragmentManager, "filter")
        }

        setupRecyclerView()
        showShimmerThenContent()
    }

    private fun showShimmerThenContent() {
        binding.shimmerSearchResults.root.isVisible = true
        binding.rvResults.isVisible = false
        binding.shimmerSearchResults.shimmerViewContainer.startShimmer()
        binding.rvResults.postDelayed({
            if (_binding != null) {
                binding.shimmerSearchResults.shimmerViewContainer.stopShimmer()
                binding.shimmerSearchResults.root.isVisible = false
                binding.rvResults.isVisible = true
            }
        }, 1000)
    }

    private fun setupRecyclerView() {
        binding.rvResults.layoutManager = GridLayoutManager(context, 2)
        binding.rvResults.adapter = ProductAdapter(
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
