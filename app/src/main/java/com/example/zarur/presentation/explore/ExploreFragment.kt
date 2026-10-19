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
import com.example.zarur.util.showInfoToast
import com.example.zarur.util.showSuccessToast
import com.google.android.material.chip.Chip
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

import com.example.zarur.util.ensureAuth

// ExploreFragment serves as the main marketplace feed.
// It operates in read-only mode for unauthenticated users, allowing product browsing and searching,
// but requires authentication (triggering a localized dialog) for actions like viewing profile or managing cart.
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
        // Navigating to profile requires user authentication
        binding.ivProfile.setOnClickListener {
            ensureAuth(viewModel.isLoggedIn()) {
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
                // Product details are accessible to all users in read-only mode
                val bundle = Bundle().apply {
                    putString("productId", product.id)
                }
                findNavController().navigate(R.id.productDetailFragment, bundle)
            },
            onAddToCartClick = { product ->
                // Adding item to cart requires authentication check
                ensureAuth(viewModel.isLoggedIn()) {
                    viewModel.toggleCart(product)
                    if (!product.isInCart) {
                        showSuccessToast(getString(R.string.msg_added_to_cart_success))
                    } else {
                        showInfoToast(getString(R.string.msg_removed_from_cart))
                    }
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
                        val displayName = when {
                            user != null && user.fullName.isNotBlank() -> user.fullName
                            user != null && user.nickname.isNotBlank() -> user.nickname
                            user != null && user.email.isNotBlank() -> user.email.substringBefore("@").replaceFirstChar { if (it.isLowerCase()) it.titlecase(
                                Locale.getDefault()) else it.toString() }
                            else -> getString(R.string.profile_name)
                        }
                        binding.tvName.text = displayName

                        if (user != null && !user.avatarUrl.isNullOrEmpty()) {
                            Glide.with(this@ExploreFragment)
                                .load(user.avatarUrl)
                                .placeholder(R.drawable.onboarding1)
                                .into(binding.ivProfile)
                        }

                        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                        val greetingRes = when (hour) {
                            in 4..11 -> R.string.greeting_morning
                            in 12..17 -> R.string.greeting_afternoon
                            else -> R.string.greeting_evening
                        }
                        binding.tvGreeting.setText(greetingRes)
                    }
                }
                launch {
                    viewModel.unreadNotificationCount.collect { count ->
                        if (count > 0) {
                            binding.tvNotificationBadge.isVisible = true
                            binding.tvNotificationBadge.text = if (count > 99) "99+" else count.toString()
                        } else {
                            binding.tvNotificationBadge.isVisible = false
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

    override fun onResume() {
        super.onResume()
        viewModel.refreshUnreadNotificationCount()
        viewModel.refreshUserProfile()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
