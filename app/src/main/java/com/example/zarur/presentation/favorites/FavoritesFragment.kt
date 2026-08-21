package com.example.zarur.presentation.favorites

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.zarur.R
import com.example.zarur.databinding.FragmentFavoritesBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FavoritesFragment : Fragment(R.layout.fragment_favorites) {

    private var _binding: FragmentFavoritesBinding? = null
    private val binding get() = _binding!!

    private lateinit var favoritesAdapter: FavoritesAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentFavoritesBinding.bind(view)

        setupRecyclerView()
        setupListeners()
    }

    private fun setupRecyclerView() {
        favoritesAdapter = FavoritesAdapter(
            onItemClick = {
                findNavController().navigate(R.id.action_favoritesFragment_to_propertyDetailFragment)
            },
            onFavoriteClick = { position ->
                showRemoveBottomSheet(position)
            }
        )

        binding.rvFavorites.apply {
            adapter = favoritesAdapter
            layoutManager = GridLayoutManager(context, 2)
        }
    }

    private fun setupListeners() {
        binding.ivViewToggle.setOnClickListener {
            val willBeGrid = !favoritesAdapter.isGridView
            favoritesAdapter.isGridView = willBeGrid

            if (willBeGrid) {
                binding.ivViewToggle.setImageResource(R.drawable.ic_grid)
                binding.rvFavorites.layoutManager = GridLayoutManager(context, 2)
            } else {
                binding.ivViewToggle.setImageResource(R.drawable.ic_list)
                binding.rvFavorites.layoutManager = LinearLayoutManager(context)
            }
        }
    }

    private fun showRemoveBottomSheet(position: Int) {
        RemoveFavoriteBottomSheet {
            // Handle removal logic here if needed
        }.show(parentFragmentManager, RemoveFavoriteBottomSheet.TAG)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
