package com.example.zarur.domain.repository

import com.example.zarur.domain.model.BookingItem
import kotlinx.coroutines.flow.Flow

interface BookingRepository {
    fun getBookings(): Flow<List<BookingItem>>
    suspend fun addBooking(booking: BookingItem)
    suspend fun cancelBooking(bookingId: String): Boolean
}
