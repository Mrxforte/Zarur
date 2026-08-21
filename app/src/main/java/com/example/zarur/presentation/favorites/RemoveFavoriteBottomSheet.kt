package com.example.zarur.presentation.favorites

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.zarur.R
import com.example.zarur.databinding.LayoutRemoveFavoriteBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class RemoveFavoriteBottomSheet(
    private val onRemove: () -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: LayoutRemoveFavoriteBottomSheetBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = LayoutRemoveFavoriteBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        binding.btnRemove.setOnClickListener {
            onRemove()
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "RemoveFavoriteBottomSheet"
    }
}
