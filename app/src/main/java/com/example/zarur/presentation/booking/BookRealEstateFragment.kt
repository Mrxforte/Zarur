package com.example.zarur.presentation.booking

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.zarur.R
import com.example.zarur.databinding.FragmentBookRealEstateBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BookRealEstateFragment : Fragment(R.layout.fragment_book_real_estate) {

    private var _binding: FragmentBookRealEstateBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentBookRealEstateBinding.bind(view)

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnContinue.setOnClickListener {
            findNavController().navigate(R.id.action_bookRealEstateFragment_to_bookingInfoFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
