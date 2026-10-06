package com.example.zarur.presentation.booking

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.zarur.R
import com.example.zarur.databinding.FragmentLeaveReviewBinding
import com.example.zarur.presentation.common.UiState
import com.example.zarur.util.showErrorToast
import com.example.zarur.util.showSuccessToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LeaveReviewFragment : Fragment(R.layout.fragment_leave_review) {

    private var _binding: FragmentLeaveReviewBinding? = null
    private val binding get() = _binding!!
    private val viewModel: LeaveReviewViewModel by viewModels()

    private var selectedRating: Float = 5.0f
    private lateinit var stars: List<ImageView>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentLeaveReviewBinding.bind(view)

        stars = listOf(binding.star1, binding.star2, binding.star3, binding.star4, binding.star5)

        setupStarRating()
        setupListeners()
        observeViewModel()
    }

    private fun setupStarRating() {
        stars.forEachIndexed { index, starImageView ->
            starImageView.setOnClickListener {
                selectedRating = (index + 1).toFloat()
                updateStarTints()
            }
        }
        updateStarTints()
    }

    private fun updateStarTints() {
        val activeColor = ContextCompat.getColor(requireContext(), R.color.yellow_uzum)
        val inactiveColor = ContextCompat.getColor(requireContext(), R.color.neutral_300)

        stars.forEachIndexed { index, imageView ->
            if (index < selectedRating.toInt()) {
                imageView.imageTintList = ColorStateList.valueOf(activeColor)
            } else {
                imageView.imageTintList = ColorStateList.valueOf(inactiveColor)
            }
        }
        binding.tvRatingScore.text = "%.1f / 5.0".format(selectedRating)
    }

    private fun setupListeners() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnSubmit.setOnClickListener {
            val productId = arguments?.getString("productId") ?: "1"
            val reviewText = binding.etReviewText.text?.toString()?.trim() ?: ""

            viewModel.submitReview(
                productId = productId,
                rating = selectedRating,
                reviewText = reviewText
            )
        }

        binding.btnMaybeLater.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.submitState.collect { state ->
                    when (state) {
                        is UiState.Loading -> {
                            binding.btnSubmit.isEnabled = false
                            binding.btnSubmit.text = "Submitting..."
                        }
                        is UiState.Success -> {
                            binding.btnSubmit.isEnabled = true
                            binding.btnSubmit.text = getString(R.string.button_submit)
                            showSuccessToast("Thank you for your review!")
                            viewModel.resetState()
                            findNavController().navigateUp()
                        }
                        is UiState.Error -> {
                            binding.btnSubmit.isEnabled = true
                            binding.btnSubmit.text = getString(R.string.button_submit)
                            showErrorToast(state.message)
                        }
                        null -> {
                            binding.btnSubmit.isEnabled = true
                            binding.btnSubmit.text = getString(R.string.button_submit)
                        }
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
