package com.example.zarur.presentation.profile

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.zarur.R
import com.example.zarur.databinding.FragmentMyBookingsBinding
import com.example.zarur.domain.model.BookingItem
import com.example.zarur.domain.model.BookingStatus
import com.example.zarur.util.showSuccessToast
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MyBookingsFragment : Fragment(R.layout.fragment_my_bookings) {

    private var _binding: FragmentMyBookingsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MyBookingsViewModel by viewModels()

    private lateinit var bookingAdapter: BookingAdapter
    private var allBookings: List<BookingItem> = emptyList()
    private var currentTabPosition = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentMyBookingsBinding.bind(view)

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        setupRecyclerView()
        setupTabLayout()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        binding.rvBookings.layoutManager = LinearLayoutManager(context)
        bookingAdapter = BookingAdapter(
            bookings = emptyList(),
            onItemClick = { _ ->
                findNavController().navigate(R.id.action_myBookingsFragment_to_productDetailFragment)
            },
            onPrimaryClick = { booking ->
                when (booking.status) {
                    BookingStatus.ACTIVE, BookingStatus.COMPLETED -> {
                        findNavController().navigate(R.id.action_myBookingsFragment_to_EReceiptFragment)
                    }
                    BookingStatus.CANCELLED -> {
                        findNavController().navigate(R.id.action_myBookingsFragment_to_productDetailFragment)
                    }
                }
            },
            onSecondaryClick = { booking ->
                when (booking.status) {
                    BookingStatus.ACTIVE -> {
                        showCancelConfirmationDialog(booking)
                    }
                    BookingStatus.COMPLETED -> {
                        findNavController().navigate(R.id.action_myBookingsFragment_to_leaveReviewFragment)
                    }
                    BookingStatus.CANCELLED -> {}
                }
            }
        )
        binding.rvBookings.adapter = bookingAdapter
    }

    private fun showCancelConfirmationDialog(booking: BookingItem) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.button_cancel_booking))
            .setMessage(getString(R.string.no_bookings_desc))
            .setPositiveButton(getString(R.string.button_cancel_booking)) { _, _ ->
                viewModel.cancelBooking(booking.id)
                showSuccessToast(getString(R.string.tab_cancelled))
            }
            .setNegativeButton(getString(R.string.button_change)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun setupTabLayout() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                currentTabPosition = tab?.position ?: 0
                filterAndDisplayBookings()
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.bookings.collect { list ->
                    allBookings = list
                    filterAndDisplayBookings()
                }
            }
        }
    }

    private fun filterAndDisplayBookings() {
        val targetStatus = when (currentTabPosition) {
            0 -> BookingStatus.ACTIVE
            1 -> BookingStatus.COMPLETED
            else -> BookingStatus.CANCELLED
        }

        val filtered = allBookings.filter { it.status == targetStatus }
        bookingAdapter.updateBookings(filtered)

        if (filtered.isEmpty()) {
            binding.rvBookings.isVisible = false
            binding.llEmptyState.isVisible = true
        } else {
            binding.rvBookings.isVisible = true
            binding.llEmptyState.isVisible = false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private class BookingAdapter(
        private var bookings: List<BookingItem>,
        private val onItemClick: (BookingItem) -> Unit,
        private val onPrimaryClick: (BookingItem) -> Unit,
        private val onSecondaryClick: (BookingItem) -> Unit
    ) : RecyclerView.Adapter<BookingAdapter.BookingViewHolder>() {

        fun updateBookings(newBookings: List<BookingItem>) {
            bookings = newBookings
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_booking, parent, false)
            return BookingViewHolder(view)
        }

        override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
            val booking = bookings[position]
            holder.bind(booking)
            holder.itemView.setOnClickListener { onItemClick(booking) }
        }

        override fun getItemCount(): Int = bookings.size

        inner class BookingViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            private val tvBookingCode = view.findViewById<TextView>(R.id.tvBookingCode)
            private val tvStatus = view.findViewById<TextView>(R.id.tvStatus)
            private val ivProperty = view.findViewById<ImageView>(R.id.ivProperty)
            private val tvPropertyName = view.findViewById<TextView>(R.id.tvPropertyName)
            private val tvPropertyLocation = view.findViewById<TextView>(R.id.tvPropertyLocation)
            private val tvDates = view.findViewById<TextView>(R.id.tvDates)
            private val tvPrice = view.findViewById<TextView>(R.id.tvPrice)
            private val btnSecondary = view.findViewById<MaterialButton>(R.id.btnSecondary)
            private val btnPrimary = view.findViewById<MaterialButton>(R.id.btnPrimary)

            fun bind(booking: BookingItem) {
                tvBookingCode.text = booking.bookingCode.ifEmpty { "Code: #${booking.id}" }
                tvPropertyName.text = booking.propertyName
                tvPropertyLocation.text = booking.location
                tvDates.text = booking.dates
                tvPrice.text = booking.formattedPrice

                ivProperty.setImageResource(R.drawable.onboarding2)

                when (booking.status) {
                    BookingStatus.ACTIVE -> {
                        tvStatus.text = itemView.context.getString(R.string.tab_active)
                        tvStatus.setBackgroundResource(R.drawable.bg_badge_capsule)
                        tvStatus.backgroundTintList = ContextCompat.getColorStateList(itemView.context, android.R.color.transparent)
                        tvStatus.setBackgroundColor(Color.parseColor("#E8F5E9"))
                        tvStatus.setTextColor(Color.parseColor("#2E7D32"))

                        btnSecondary.visibility = View.VISIBLE
                        btnSecondary.text = itemView.context.getString(R.string.button_cancel_booking)

                        btnPrimary.visibility = View.VISIBLE
                        btnPrimary.text = itemView.context.getString(R.string.button_e_receipt)
                    }
                    BookingStatus.COMPLETED -> {
                        tvStatus.text = itemView.context.getString(R.string.tab_completed)
                        tvStatus.setBackgroundResource(R.drawable.bg_badge_capsule)
                        tvStatus.setBackgroundColor(Color.parseColor("#E3F2FD"))
                        tvStatus.setTextColor(Color.parseColor("#1565C0"))

                        btnSecondary.visibility = View.VISIBLE
                        btnSecondary.text = itemView.context.getString(R.string.button_leave_review)

                        btnPrimary.visibility = View.VISIBLE
                        btnPrimary.text = itemView.context.getString(R.string.button_e_receipt)
                    }
                    BookingStatus.CANCELLED -> {
                        tvStatus.text = itemView.context.getString(R.string.tab_cancelled)
                        tvStatus.setBackgroundResource(R.drawable.bg_badge_capsule)
                        tvStatus.setBackgroundColor(Color.parseColor("#FFEBEE"))
                        tvStatus.setTextColor(Color.parseColor("#C62828"))

                        btnSecondary.visibility = View.GONE

                        btnPrimary.visibility = View.VISIBLE
                        btnPrimary.text = itemView.context.getString(R.string.button_rebook)
                    }
                }

                btnPrimary.setOnClickListener { onPrimaryClick(booking) }
                btnSecondary.setOnClickListener { onSecondaryClick(booking) }
            }
        }
    }
}
