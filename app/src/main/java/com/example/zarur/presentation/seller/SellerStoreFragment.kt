package com.example.zarur.presentation.seller

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.example.zarur.R
import com.example.zarur.databinding.FragmentSellerStoreBinding
import com.example.zarur.domain.usecase.IsLoggedInUseCase
import com.example.zarur.presentation.common.ProductAdapter
import com.example.zarur.util.ensureAuth
import com.example.zarur.util.showAuthRequiredDialog
import com.example.zarur.util.showInfoToast
import com.example.zarur.util.showSuccessToast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SellerStoreFragment : Fragment(R.layout.fragment_seller_store) {

    private var _binding: FragmentSellerStoreBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SellerStoreViewModel by viewModels()

    @Inject
    lateinit var isLoggedInUseCase: IsLoggedInUseCase

    private lateinit var sellerProductAdapter: ProductAdapter
    private lateinit var relatedProductAdapter: ProductAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSellerStoreBinding.bind(view)

        val sellerId = arguments?.getString("sellerId") ?: ""
        val sellerName = arguments?.getString("sellerName") ?: getString(R.string.official_seller_store)
        val sellerAvatar = arguments?.getString("sellerAvatar")

        if (sellerId.isNotEmpty()) {
            viewModel.loadSeller(sellerId)
        }

        bindSellerHeader(sellerName, sellerAvatar)
        setupRecyclerViews()
        setupListeners()
        observeViewModel()
    }

    private fun bindSellerHeader(name: String, avatarUrl: String?) {
        binding.tvToolbarTitle.text = name
        binding.tvSellerName.text = name

        if (!avatarUrl.isNullOrEmpty()) {
            Glide.with(binding.ivSellerAvatar)
                .load(avatarUrl)
                .placeholder(R.drawable.onboarding2)
                .into(binding.ivSellerAvatar)
        } else {
            binding.ivSellerAvatar.setImageResource(R.drawable.onboarding2)
        }
    }

    private fun setupRecyclerViews() {
        sellerProductAdapter = ProductAdapter(
            onItemClick = { product ->
                val bundle = Bundle().apply { putString("productId", product.id) }
                findNavController().navigate(R.id.productDetailFragment, bundle)
            },
            onAddToCartClick = { product ->
                ensureAuth(isLoggedInUseCase()) {
                    viewModel.toggleCart(product)
                    if (!product.isInCart) {
                        showSuccessToast(getString(R.string.msg_added_to_cart_success))
                    } else {
                        showInfoToast(getString(R.string.msg_removed_from_cart))
                    }
                }
            }
        )

        binding.rvSellerProducts.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvSellerProducts.adapter = sellerProductAdapter

        relatedProductAdapter = ProductAdapter(
            onItemClick = { product ->
                val bundle = Bundle().apply { putString("productId", product.id) }
                findNavController().navigate(R.id.productDetailFragment, bundle)
            },
            onAddToCartClick = { product ->
                ensureAuth(isLoggedInUseCase()) {
                    viewModel.toggleCart(product)
                    if (!product.isInCart) {
                        showSuccessToast(getString(R.string.msg_added_to_cart_success))
                    } else {
                        showInfoToast(getString(R.string.msg_removed_from_cart))
                    }
                }
            }
        )

        binding.rvRelatedProducts.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvRelatedProducts.adapter = relatedProductAdapter
    }

    private fun setupListeners() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnCallSeller.setOnClickListener {
            ensureAuth(isLoggedInUseCase()) {
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:+998901234567"))
                startActivity(intent)
            }
        }
    }

    private fun observeViewModel() {
        binding.shimmerSellerProducts.root.isVisible = true
        binding.rvSellerProducts.isVisible = false
        binding.shimmerSellerProducts.shimmerViewContainer.startShimmer()
        var isFirstEmit = true
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.sellerProducts.collect { products ->
                        sellerProductAdapter.submitList(products)
                        binding.tvProductCount.text = getString(R.string.seller_product_count, products.size)
                        if (isFirstEmit) {
                            isFirstEmit = false
                            delay(1000)
                        }
                        binding.shimmerSellerProducts.shimmerViewContainer.stopShimmer()
                        binding.shimmerSellerProducts.root.isVisible = false
                        binding.rvSellerProducts.isVisible = true
                    }
                }
                launch {
                    viewModel.relatedProducts.collect { products ->
                        relatedProductAdapter.submitList(products)
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
