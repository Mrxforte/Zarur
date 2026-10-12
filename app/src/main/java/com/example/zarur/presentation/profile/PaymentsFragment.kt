package com.example.zarur.presentation.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.zarur.databinding.FragmentPaymentsBinding
import com.example.zarur.domain.model.PaymentMethod
import com.example.zarur.util.showInfoToast
import com.example.zarur.util.showSuccessToast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PaymentsFragment : Fragment() {

    private var _binding: FragmentPaymentsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PaymentViewModel by activityViewModels()

    private lateinit var cardsAdapter: CardsAdapter
    private lateinit var walletsAdapter: WalletsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPaymentsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupRecyclerViews()
        setupAddCardButtons()
        observeState()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupRecyclerViews() {
        cardsAdapter = CardsAdapter(
            isSelectionMode = false,
            onCardClick = { card ->
                if (!card.isDefault) {
                    viewModel.setDefaultCard(card.id)
                    showSuccessToast("Primary payment card updated")
                }
            },
            onDeleteClick = { card ->
                confirmDeleteCard(card)
            }
        )

        walletsAdapter = WalletsAdapter(
            isSelectionMode = false,
            onWalletClick = { wallet ->
                toggleWallet(wallet)
            },
            onActionClick = { wallet ->
                toggleWallet(wallet)
            }
        )

        binding.rvCreditCards.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = cardsAdapter
        }

        binding.rvDigitalWallets.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = walletsAdapter
        }
    }

    private fun confirmDeleteCard(card: PaymentMethod) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Payment Card")
            .setMessage("Are you sure you want to remove ${card.title} (${card.maskedNumber})?")
            .setPositiveButton("Delete") { dialog, _ ->
                viewModel.deleteCard(card.id)
                showInfoToast("Payment card removed")
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun toggleWallet(wallet: PaymentMethod) {
        viewModel.toggleWallet(wallet.id)
        if (wallet.isConnected) {
            showInfoToast("${wallet.title} disconnected")
        } else {
            showSuccessToast("${wallet.title} connected")
        }
    }

    private fun setupAddCardButtons() {
        val openAddCard = {
            val bottomSheet = AddCardBottomSheet()
            bottomSheet.show(childFragmentManager, "AddCardBottomSheet")
        }

        binding.btnAddCard.setOnClickListener { openAddCard() }
        binding.btnHeaderAddCard.setOnClickListener { openAddCard() }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    cardsAdapter.submitList(state.cards)
                    walletsAdapter.submitList(state.wallets)

                    binding.tvEmptyCards.isVisible = state.cards.isEmpty()
                    binding.tvEmptyWallets.isVisible = state.wallets.isEmpty()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
