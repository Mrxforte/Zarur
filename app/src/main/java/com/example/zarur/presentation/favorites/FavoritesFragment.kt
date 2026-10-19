package com.example.zarur.presentation.favorites

import android.os.Bundle
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
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.zarur.R
import com.example.zarur.databinding.FragmentFavoritesBinding
import com.example.zarur.domain.usecase.IsLoggedInUseCase
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

import com.example.zarur.util.ensureAuth
import com.example.zarur.util.showAuthRequiredDialog

@AndroidEntryPoint
class FavoritesFragment : Fragment(R.layout.fragment_favorites) {

    private var _binding: FragmentFavoritesBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: FavoritesViewModel by viewModels()

    @Inject
    lateinit var isLoggedInUseCase: IsLoggedInUseCase

    private lateinit var favoritesAdapter: FavoritesAdapter
    private var currentTab = 0 // 0: Favorites, 1: Cart

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentFavoritesBinding.bind(view)

        if (!isLoggedInUseCase()) {
            showAuthRequiredDialog()
        }

        setupRecyclerView()
        setupInsets()
        setupListeners()
        observeViewModel()
    }

    private fun setupInsets() {
        val initialPaddingTop = binding.appBarLayout.paddingTop
        ViewCompat.setOnApplyWindowInsetsListener(binding.appBarLayout) { v, insets ->
            val statusBars = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            v.updatePadding(top = initialPaddingTop + statusBars.top)
            insets
        }
    }
    
    private fun observeViewModel() {
        binding.shimmerFavorites.root.isVisible = true
        binding.rvFavorites.isVisible = false
        binding.shimmerFavorites.shimmerViewContainer.startShimmer()

        var isFirstEmit = true
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.favoriteProducts.collectLatest { favorites ->
                        if (currentTab == 0) {
                            binding.tvFavoritesCount.text = getString(R.string.favorites_count_text, favorites.size)
                            favoritesAdapter.submitList(favorites)
                            if (isFirstEmit) {
                                isFirstEmit = false
                                delay(1000)
                            }
                            hideShimmer()
                        }
                    }
                }
                launch {
                    viewModel.cartProducts.collectLatest { cart ->
                        if (currentTab == 1) {
                            binding.tvFavoritesCount.text = getString(R.string.cart_count_text, cart.size)
                            favoritesAdapter.submitList(cart)
                            hideShimmer()
                        }
                    }
                }
            }
        }
    }

    private fun hideShimmer() {
        binding.shimmerFavorites.shimmerViewContainer.stopShimmer()
        binding.shimmerFavorites.root.isVisible = false
        binding.rvFavorites.isVisible = true
    }

    private fun setupRecyclerView() {
        favoritesAdapter = FavoritesAdapter(
            onItemClick = { product ->
                val bundle = Bundle().apply {
                    putString("productId", product.id)
                }
                findNavController().navigate(R.id.productDetailFragment, bundle)
            },
            onFavoriteClick = { product ->
                ensureAuth(isLoggedInUseCase()) {
                    if (currentTab == 0) {
                        viewModel.toggleFavorite(product)
                    } else {
                        viewModel.removeFromCart(product)
                    }
                }
            },
            onAddToCartClick = { product ->
                ensureAuth(isLoggedInUseCase()) {
                    viewModel.toggleCart(product)
                }
            }
        )

        binding.rvFavorites.apply {
            adapter = favoritesAdapter
            layoutManager = GridLayoutManager(context, 2)
        }
    }

    private fun setupListeners() {
        binding.ivViewToggle.setOnClickListener {
            val willBeGrid = !favoritesAdapter.isGridView
            favoritesAdapter.isGridView = willBeGrid

            if (willBeGrid) {
                binding.ivViewToggle.setImageResource(R.drawable.ic_grid)
                binding.rvFavorites.layoutManager = GridLayoutManager(context, 2)
            } else {
                binding.ivViewToggle.setImageResource(R.drawable.ic_list)
                binding.rvFavorites.layoutManager = LinearLayoutManager(context)
            }
        }

        binding.tabLayoutFavoritesCart.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                currentTab = tab?.position ?: 0
                updateCurrentList()
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun updateCurrentList() {
        if (currentTab == 0) {
            val favorites = viewModel.favoriteProducts.value
            binding.tvFavoritesCount.text = getString(R.string.favorites_count_text, favorites.size)
            favoritesAdapter.submitList(favorites)
        } else {
            val cart = viewModel.cartProducts.value
            binding.tvFavoritesCount.text = getString(R.string.cart_count_text, cart.size)
            favoritesAdapter.submitList(cart)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
