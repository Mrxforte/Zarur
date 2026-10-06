package com.example.zarur.presentation.seller

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.zarur.R
import com.example.zarur.databinding.FragmentSellerDashboardBinding
import com.example.zarur.domain.usecase.IsLoggedInUseCase
import com.example.zarur.presentation.common.ProductAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SellerDashboardFragment : Fragment(R.layout.fragment_seller_dashboard) {

    private var _binding: FragmentSellerDashboardBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SellerViewModel by viewModels()
    private lateinit var adapter: ProductAdapter

    @Inject
    lateinit var isLoggedInUseCase: IsLoggedInUseCase

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!isLoggedInUseCase()) {
            findNavController().navigate(R.id.authHubFragment)
            return
        }

        _binding = FragmentSellerDashboardBinding.bind(view)

        setupToolbar()
        setupRecyclerView()
        setupListeners()
        observeViewModel()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupRecyclerView() {
        adapter = ProductAdapter(
            onItemClick = { product ->
                val bundle = Bundle().apply {
                    putString("productId", product.id)
                }
                findNavController().navigate(R.id.productDetailFragment, bundle)
            }
        )
        binding.rvMyProducts.layoutManager = GridLayoutManager(context, 2)
        binding.rvMyProducts.adapter = adapter
    }

    private fun setupListeners() {
        binding.fabAdd.setOnClickListener {
            if (!isLoggedInUseCase()) {
                findNavController().navigate(R.id.authHubFragment)
            } else {
                if (findNavController().currentDestination?.id == R.id.sellerDashboardFragment) {
                    findNavController().navigate(R.id.action_sellerDashboardFragment_to_createProductFragment)
                }
            }
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.sellerProducts.collect { products ->
                    adapter.submitList(products)

                    val activeCount = products.count { !it.isSold }
                    val soldCount = products.count { it.isSold }

                    binding.tvActiveCount.text = activeCount.toString()
                    binding.tvSoldCount.text = soldCount.toString()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
