package com.example.zarur.presentation.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.zarur.databinding.FragmentPaymentsBinding
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
                viewModel.setDefaultCard(card.id)
            },
            onDeleteClick = { card ->
                viewModel.deleteCard(card.id)
            }
        )

        walletsAdapter = WalletsAdapter(
            isSelectionMode = false,
            onWalletClick = { wallet ->
                viewModel.toggleWallet(wallet.id)
            },
            onActionClick = { wallet ->
                viewModel.toggleWallet(wallet.id)
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
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
