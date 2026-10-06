package com.example.zarur.presentation.profile

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.zarur.R
import com.example.zarur.databinding.FragmentLanguageBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LanguageFragment : Fragment(R.layout.fragment_language) {

    private var _binding: FragmentLanguageBinding? = null
    private val binding get() = _binding!!
    private val viewModel: LanguageViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentLanguageBinding.bind(view)

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        setupSelection()
        observeLanguage()
    }

    private fun setupSelection() {
        binding.containerRu.setOnClickListener { 
            viewModel.selectLanguage("ru")
            requireActivity().recreate()
        }
        binding.containerUz.setOnClickListener { 
            viewModel.selectLanguage("uz")
            requireActivity().recreate()
        }
        binding.containerEn.setOnClickListener { 
            viewModel.selectLanguage("en")
            requireActivity().recreate()
        }
    }

    private fun observeLanguage() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.selectedLanguage.collect { language ->
                    binding.rbRu.isChecked = language == "ru"
                    binding.rbUz.isChecked = language == "uz"
                    binding.rbEn.isChecked = language == "en"
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
