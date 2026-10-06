package com.example.zarur.presentation.booking

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.zarur.util.showInfoToast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.zarur.R
import com.example.zarur.databinding.FragmentSelectPaymentBinding
import com.example.zarur.presentation.profile.AddCardBottomSheet
import com.example.zarur.presentation.profile.CardsAdapter
import com.example.zarur.presentation.profile.PaymentViewModel
import com.example.zarur.presentation.profile.WalletsAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SelectPaymentFragment : Fragment() {

    private var _binding: FragmentSelectPaymentBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PaymentViewModel by activityViewModels()

    private lateinit var cardsAdapter: CardsAdapter
    private lateinit var walletsAdapter: WalletsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSelectPaymentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupRecyclerViews()
        setupAddCardButton()
        setupContinueButton()
        observeState()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupRecyclerViews() {
        cardsAdapter = CardsAdapter(
            isSelectionMode = true,
            onCardClick = { card ->
                viewModel.selectMethod(card.id)
                walletsAdapter.setSelectedId(null)
            }
        )

        walletsAdapter = WalletsAdapter(
            isSelectionMode = true,
            onWalletClick = { wallet ->
                viewModel.selectMethod(wallet.id)
                cardsAdapter.setSelectedId(null)
            }
        )

        binding.rvSelectCards.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = cardsAdapter
        }

        binding.rvSelectWallets.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = walletsAdapter
        }
    }

    private fun setupAddCardButton() {
        binding.btnAddNewCard.setOnClickListener {
            val bottomSheet = AddCardBottomSheet()
            bottomSheet.show(childFragmentManager, "AddCardBottomSheet")
        }
    }

    private fun setupContinueButton() {
        binding.btnContinue.setOnClickListener {
            val selectedId = viewModel.uiState.value.selectedMethodId
            if (selectedId.isNullOrEmpty()) {
                showInfoToast("Iltimos, to'lov usulini tanlang")
            } else {
                if (findNavController().currentDestination?.id == R.id.selectPaymentFragment) {
                    findNavController().navigate(R.id.action_selectPaymentFragment_to_reviewSummaryFragment)
                }
            }
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    val selectedId = state.selectedMethodId
                    val isCardSelected = state.cards.any { it.id == selectedId }

                    cardsAdapter.submitListWithSelection(state.cards, if (isCardSelected) selectedId else null)
                    walletsAdapter.submitListWithSelection(state.wallets, if (!isCardSelected && state.wallets.any { it.id == selectedId }) selectedId else null)

                    binding.progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE
                    binding.btnContinue.isEnabled = !state.isLoading
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
