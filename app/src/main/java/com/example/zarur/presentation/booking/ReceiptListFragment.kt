package com.example.zarur.presentation.booking

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.zarur.R
import com.example.zarur.databinding.FragmentReceiptListBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ReceiptListFragment : Fragment(R.layout.fragment_receipt_list) {

    private var _binding: FragmentReceiptListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ReceiptViewModel by viewModels()
    private lateinit var adapter: ReceiptAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentReceiptListBinding.bind(view)

        setupToolbar()
        setupRecyclerView()
        observeViewModel()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupRecyclerView() {
        adapter = ReceiptAdapter { receipt ->
            val bundle = Bundle().apply {
                putSerializable("receipt", receipt)
            }
            if (findNavController().currentDestination?.id == R.id.receiptListFragment) {
                findNavController().navigate(R.id.action_receiptListFragment_to_EReceiptFragment, bundle)
            }
        }

        binding.rvReceipts.layoutManager = LinearLayoutManager(context)
        binding.rvReceipts.adapter = adapter
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.receipts.collect { receipts ->
                    adapter.submitList(receipts)
                    binding.layoutEmpty.isVisible = receipts.isEmpty()
                    binding.rvReceipts.isVisible = receipts.isNotEmpty()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
