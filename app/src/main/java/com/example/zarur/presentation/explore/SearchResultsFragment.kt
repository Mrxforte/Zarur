package com.example.zarur.presentation.explore

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.zarur.R
import com.example.zarur.databinding.FragmentSearchResultsBinding
import com.example.zarur.presentation.common.PropertyAdapter
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
    }

    private fun setupRecyclerView() {
        binding.rvResults.layoutManager = LinearLayoutManager(context)
        binding.rvResults.adapter = PropertyAdapter {
            findNavController().navigate(R.id.action_searchResultsFragment_to_propertyDetailFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
