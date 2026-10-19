package com.example.zarur.presentation.booking

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
import com.example.zarur.R
import com.example.zarur.databinding.FragmentSelectPaymentBinding
import com.example.zarur.domain.usecase.IsLoggedInUseCase
import com.example.zarur.presentation.profile.AddCardBottomSheet
import com.example.zarur.presentation.profile.CardsAdapter
import com.example.zarur.presentation.profile.PaymentViewModel
import com.example.zarur.presentation.profile.WalletsAdapter
import com.example.zarur.util.showInfoToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SelectPaymentFragment : Fragment() {

    private var _binding: FragmentSelectPaymentBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var isLoggedInUseCase: IsLoggedInUseCase

    private val viewModel: PaymentViewModel by activityViewModels()
    private val bookingFlowViewModel: BookingFlowViewModel by activityViewModels()

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

        if (!isLoggedInUseCase()) {
            showInfoToast(getString(R.string.msg_sign_in_to_continue))
            findNavController().navigate(R.id.action_global_authHubFragment)
            return
        }

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
            }
        )

        walletsAdapter = WalletsAdapter(
            isSelectionMode = true,
            onWalletClick = { wallet ->
                viewModel.selectMethod(wallet.id)
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
                showInfoToast(getString(R.string.title_select_payment))
            } else {
                val state = viewModel.uiState.value
                val selectedMethod = state.cards.find { it.id == selectedId }
                    ?: state.wallets.find { it.id == selectedId }
                    ?: state.cards.find { it.isDefault }
                selectedMethod?.let {
                    bookingFlowViewModel.setPaymentMethod(
                        methodName = it.formattedDisplayNumber.ifEmpty { "Card" },
                        iconRes = it.iconResId
                    )
                }
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

                    val cardsWithSelection = state.cards.map { card ->
                        card.copy(isSelected = (card.id == selectedId || (selectedId == null && card.isDefault)))
                    }
                    val walletsWithSelection = state.wallets.map { wallet ->
                        wallet.copy(isSelected = (wallet.id == selectedId))
                    }

                    cardsAdapter.submitList(cardsWithSelection)
                    walletsAdapter.submitList(walletsWithSelection)

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
