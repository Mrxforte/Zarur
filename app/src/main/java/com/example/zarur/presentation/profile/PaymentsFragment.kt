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
import com.example.zarur.R
import com.example.zarur.databinding.FragmentPaymentsBinding
import com.example.zarur.domain.model.PaymentMethod
import com.example.zarur.util.showInfoToast
import com.example.zarur.util.showSuccessToast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.example.zarur.domain.usecase.IsLoggedInUseCase
import com.example.zarur.util.ensureAuth
import com.example.zarur.util.showAuthRequiredDialog
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PaymentsFragment : Fragment() {

    private var _binding: FragmentPaymentsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PaymentViewModel by activityViewModels()

    @Inject
    lateinit var isLoggedInUseCase: IsLoggedInUseCase

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

        if (!isLoggedInUseCase()) {
            showAuthRequiredDialog()
        }

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
                    showSuccessToast(getString(R.string.msg_primary_card_updated))
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
            .setTitle(getString(R.string.title_remove_favorite))
            .setMessage("${card.title} (${card.maskedNumber})?")
            .setPositiveButton(getString(R.string.button_yes_remove)) { dialog, _ ->
                viewModel.deleteCard(card.id)
                showInfoToast(getString(R.string.msg_card_removed))
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.button_cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun toggleWallet(wallet: PaymentMethod) {
        viewModel.toggleWallet(wallet.id)
        if (wallet.isConnected) {
            showInfoToast(getString(R.string.msg_wallet_disconnected, wallet.title))
        } else {
            showSuccessToast(getString(R.string.msg_wallet_connected, wallet.title))
        }
    }

    private fun setupAddCardButtons() {
        val openAddCard = {
            ensureAuth(isLoggedInUseCase()) {
                val bottomSheet = AddCardBottomSheet()
                bottomSheet.show(childFragmentManager, "AddCardBottomSheet")
            }
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
