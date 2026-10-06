package com.example.zarur.presentation.seller

import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.zarur.R
import com.example.zarur.databinding.FragmentCreateProductBinding
import com.example.zarur.domain.model.ProductCategory
import com.example.zarur.domain.usecase.IsLoggedInUseCase
import com.example.zarur.presentation.common.UiState
import com.example.zarur.util.showErrorToast
import com.example.zarur.util.showSuccessToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class CreateProductFragment : Fragment(R.layout.fragment_create_product) {

    private var _binding: FragmentCreateProductBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SellerViewModel by viewModels()

    @Inject
    lateinit var isLoggedInUseCase: IsLoggedInUseCase

    private val imagePicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            binding.ivSelectedPhoto.visibility = View.VISIBLE
            binding.llPhotoPlaceholder.visibility = View.GONE
            Glide.with(this).load(it).into(binding.ivSelectedPhoto)
            viewModel.setSelectedPhoto(it.toString())
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!isLoggedInUseCase()) {
            findNavController().navigate(R.id.authHubFragment)
            return
        }

        _binding = FragmentCreateProductBinding.bind(view)

        setupToolbar()
        setupCategoryDropdown()
        setupListeners()
        observeViewModel()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupCategoryDropdown() {
        val categories = ProductCategory.all.map { 
            it.id.replaceFirstChar { char -> char.uppercase() }
        }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, categories)
        binding.etCategory.setAdapter(adapter)
    }

    private fun setupListeners() {
        binding.cvAddPhoto.setOnClickListener {
            imagePicker.launch("image/*")
        }

        binding.etImageUrl.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val url = s?.toString()?.trim() ?: ""
                if (url.isNotEmpty()) {
                    binding.ivSelectedPhoto.visibility = View.VISIBLE
                    binding.llPhotoPlaceholder.visibility = View.GONE
                    Glide.with(this@CreateProductFragment).load(url).into(binding.ivSelectedPhoto)
                    viewModel.setSelectedPhoto(url)
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.btnSubmit.setOnClickListener {
            if (!isLoggedInUseCase()) {
                findNavController().navigate(R.id.authHubFragment)
                return@setOnClickListener
            }

            val title = binding.etTitle.text?.toString()?.trim() ?: ""
            val category = binding.etCategory.text?.toString()?.trim() ?: ""
            val priceStr = binding.etPrice.text?.toString()?.trim() ?: ""
            val description = binding.etDescription.text?.toString()?.trim() ?: ""

            if (title.isEmpty()) {
                binding.etTitle.error = getString(R.string.ad_title)
                return@setOnClickListener
            }

            if (category.isEmpty()) {
                binding.etCategory.error = getString(R.string.label_category)
                return@setOnClickListener
            }

            val price = priceStr.toDoubleOrNull() ?: 0.0
            if (price <= 0) {
                binding.etPrice.error = getString(R.string.price)
                return@setOnClickListener
            }

            viewModel.uploadProduct(
                title = title,
                category = category,
                price = price,
                description = description
            )
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uploadState.collect { state ->
                    when (state) {
                        is UiState.Loading -> {
                            binding.btnSubmit.isEnabled = false
                            binding.btnSubmit.text = getString(R.string.post_advertisement)
                        }
                        is UiState.Success -> {
                            binding.btnSubmit.isEnabled = true
                            binding.btnSubmit.text = getString(R.string.post_advertisement)
                            showSuccessToast(getString(R.string.post_advertisement))
                            viewModel.resetUploadState()
                            findNavController().navigateUp()
                        }
                        is UiState.Error -> {
                            binding.btnSubmit.isEnabled = true
                            binding.btnSubmit.text = getString(R.string.post_advertisement)
                            if (state.message == "AUTH_REQUIRED") {
                                findNavController().navigate(R.id.authHubFragment)
                            } else {
                                showErrorToast(state.message)
                            }
                        }
                        null -> {
                            binding.btnSubmit.isEnabled = true
                            binding.btnSubmit.text = getString(R.string.post_advertisement)
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
