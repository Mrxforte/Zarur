package com.example.zarur.presentation.profile

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.zarur.databinding.BottomSheetAddCardBinding
import com.example.zarur.domain.model.CardBrand
import com.example.zarur.util.showErrorToast
import com.example.zarur.util.showSuccessToast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.launch

class AddCardBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetAddCardBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PaymentViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetAddCardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.clearMessages()
        setupLivePreviewCard()
        setupSubmitButton()
        observeState()
    }

    private fun setupLivePreviewCard() {
        val preview = binding.previewCard
        preview.btnDeleteCard.visibility = View.GONE
        preview.tvDefaultBadge.visibility = View.GONE

        // Card Holder Live Updates
        binding.etCardHolder.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                preview.tvCardHolder.text = if (!s.isNullOrEmpty()) s.toString().uppercase() else "CARD HOLDER"
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Card Number Live Formatting & Brand Detection
        binding.etCardNumber.addTextChangedListener(object : TextWatcher {
            private var isFormatting = false

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (isFormatting) return
                val raw = s.toString().replace("\\s+".toRegex(), "")
                val brand = CardBrand.detectFromNumber(raw)

                preview.clCardBackground.setBackgroundResource(brand.bgResId)
                preview.ivBrandLogo.setImageResource(brand.iconResId)

                val formattedPreview = if (raw.isNotEmpty()) {
                    raw.chunked(4).joinToString(" ")
                } else {
                    "•••• •••• •••• ••••"
                }
                preview.tvCardNumber.text = formattedPreview
            }

            override fun afterTextChanged(s: Editable?) {
                if (isFormatting || s == null) return
                isFormatting = true
                val clean = s.toString().replace("\\s+".toRegex(), "").take(16)
                val formatted = clean.chunked(4).joinToString(" ")
                if (formatted != s.toString()) {
                    s.replace(0, s.length, formatted)
                }
                isFormatting = false
            }
        })

        // Expiry Date Live Formatting (MM/YY)
        binding.etExpiry.addTextChangedListener(object : TextWatcher {
            private var isFormatting = false

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val input = s.toString()
                preview.tvExpiryDate.text = if (input.isNotEmpty()) input else "MM/YY"
            }

            override fun afterTextChanged(s: Editable?) {
                if (isFormatting || s == null) return
                isFormatting = true
                val clean = s.toString().replace("/", "").take(4)
                val formatted = if (clean.length >= 3) {
                    "${clean.substring(0, 2)}/${clean.substring(2)}"
                } else {
                    clean
                }
                if (formatted != s.toString()) {
                    s.replace(0, s.length, formatted)
                }
                isFormatting = false
            }
        })
    }

    private fun setupSubmitButton() {
        binding.btnAddCardSubmit.setOnClickListener {
            val holder = binding.etCardHolder.text.toString().trim()
            val number = binding.etCardNumber.text.toString().trim()
            val expiry = binding.etExpiry.text.toString().trim()
            val cvv = binding.etCvv.text.toString().trim()
            val isDefault = binding.cbSetDefault.isChecked

            val cleanNum = number.replace("\\s+".toRegex(), "")
            if (holder.isBlank()) {
                showErrorToast("Please enter cardholder name")
                return@setOnClickListener
            }
            if (cleanNum.length !in 15..16) {
                showErrorToast("Card number must be 15 or 16 digits")
                return@setOnClickListener
            }
            if (!expiry.matches("^\\d{2}/\\d{2}\$".toRegex())) {
                showErrorToast("Expiry date must be in MM/YY format")
                return@setOnClickListener
            }
            if (cvv.length !in 3..4) {
                showErrorToast("CVV must be 3 or 4 digits")
                return@setOnClickListener
            }

            viewModel.addCard(
                cardHolderName = holder,
                cardNumber = cleanNum,
                expiryDate = expiry,
                cvv = cvv,
                isDefault = isDefault
            )
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    binding.progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE
                    binding.btnAddCardSubmit.isEnabled = !state.isLoading

                    state.error?.let { err ->
                        showErrorToast(err)
                        viewModel.clearMessages()
                    }

                    state.addCardSuccessMessage?.let { msg ->
                        showSuccessToast(msg)
                        viewModel.clearMessages()
                        dismiss()
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        viewModel.clearMessages()
        super.onDestroyView()
        _binding = null
    }
}
