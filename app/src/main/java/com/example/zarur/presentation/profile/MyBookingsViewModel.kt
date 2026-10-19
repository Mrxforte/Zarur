package com.example.zarur.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zarur.domain.model.BookingItem
import com.example.zarur.domain.repository.BookingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyBookingsViewModel @Inject constructor(
    private val bookingRepository: BookingRepository
) : ViewModel() {

    private val _bookings = MutableStateFlow<List<BookingItem>>(emptyList())
    val bookings: StateFlow<List<BookingItem>> = _bookings.asStateFlow()

    init {
        loadBookings()
    }

    fun loadBookings() {
        viewModelScope.launch {
            bookingRepository.getBookings().collect { list ->
                _bookings.value = list
            }
        }
    }

    fun cancelBooking(bookingId: String) {
        viewModelScope.launch {
            bookingRepository.cancelBooking(bookingId)
        }
    }
}
