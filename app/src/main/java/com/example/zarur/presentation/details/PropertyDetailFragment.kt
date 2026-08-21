package com.example.zarur.presentation.details

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.zarur.R
import com.example.zarur.databinding.FragmentPropertyDetailBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PropertyDetailFragment : Fragment(R.layout.fragment_property_detail) {

    private var _binding: FragmentPropertyDetailBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentPropertyDetailBinding.bind(view)

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnBooking.setOnClickListener {
            findNavController().navigate(R.id.action_propertyDetailFragment_to_bookRealEstateFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
