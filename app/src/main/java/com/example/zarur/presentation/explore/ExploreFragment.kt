package com.example.zarur.presentation.explore

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.example.zarur.R
import com.example.zarur.databinding.FragmentExploreBinding
import com.example.zarur.presentation.common.ProductAdapter
import com.google.android.material.chip.Chip
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ExploreFragment : Fragment(R.layout.fragment_explore) {

    private var _binding: FragmentExploreBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ExploreViewModel by viewModels()
    private lateinit var adapter: ProductAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentExploreBinding.bind(view)

        setupRecyclerView()
        setupInsets()
        setupListeners()
        observeViewModel()
    }

    private fun setupInsets() {
        val initialPaddingTop = binding.headerLayout.paddingTop
        ViewCompat.setOnApplyWindowInsetsListener(binding.headerLayout) { v, insets ->
            val statusBars = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            v.updatePadding(top = initialPaddingTop + statusBars.top)
            insets
        }
    }

    private fun setupListeners() {
        binding.ivProfile.setOnClickListener {
            if (!viewModel.isLoggedIn()) {
                findNavController().navigate(R.id.authHubFragment)
            } else {
                findNavController().navigate(R.id.action_exploreFragment_to_profileFragment)
            }
        }

        binding.ivSearch.setOnClickListener {
            binding.cvSearchBar.isVisible = !binding.cvSearchBar.isVisible
            if (binding.cvSearchBar.isVisible) {
                binding.etSearch.requestFocus()
            } else {
                viewModel.search("")
            }
        }

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.search(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.chipGroupCategories.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isEmpty()) {
                viewModel.setCategory(null)
            } else {
                val chip = group.findViewById<Chip>(checkedIds.first())
                viewModel.setCategory(chip.text.toString())
            }
        }

        binding.ivNotification.setOnClickListener {
            findNavController().navigate(R.id.action_exploreFragment_to_notificationFragment)
        }

        binding.ivFilter.setOnClickListener {
            FilterBottomSheet().show(parentFragmentManager, "filter")
        }
    }

    private fun setupRecyclerView() {
        adapter = ProductAdapter(
            onItemClick = { product ->
                val bundle = Bundle().apply {
                    putString("productId", product.id)
                }
                findNavController().navigate(R.id.productDetailFragment, bundle)
            },
            onAddToCartClick = { product ->
                if (!viewModel.isLoggedIn()) {
                    findNavController().navigate(R.id.authHubFragment)
                } else {
                    viewModel.addToCart(product.id)
                }
            }
        )

        binding.rvRecommendation.layoutManager = GridLayoutManager(context, 2)
        binding.rvRecommendation.adapter = adapter
    }

    private fun observeViewModel() {
        binding.shimmerExplore.root.isVisible = true
        binding.rvRecommendation.isVisible = false
        binding.shimmerExplore.shimmerViewContainer.startShimmer()

        var isFirstEmit = true
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.userProfile.collect { user ->
                        if (user != null) {
                            if (user.fullName.isNotBlank()) {
                                binding.tvName.text = user.fullName
                            } else if (user.email.isNotBlank()) {
                                binding.tvName.text = user.email
                            }
                            if (!user.avatarUrl.isNullOrEmpty()) {
                                Glide.with(this@ExploreFragment)
                                    .load(user.avatarUrl)
                                    .placeholder(R.drawable.onboarding1)
                                    .into(binding.ivProfile)
                            }
                        }
                    }
                }
                launch {
                    viewModel.properties.collect { products ->
                        adapter.submitList(products)
                        if (products.isNotEmpty()) {
                            if (isFirstEmit) {
                                isFirstEmit = false
                                delay(1000)
                            }
                            binding.shimmerExplore.shimmerViewContainer.stopShimmer()
                            binding.shimmerExplore.root.isVisible = false
                            binding.rvRecommendation.isVisible = true
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
