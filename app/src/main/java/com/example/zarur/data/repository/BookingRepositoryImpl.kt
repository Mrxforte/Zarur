package com.example.zarur.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.zarur.domain.model.BookingItem
import com.example.zarur.domain.model.BookingStatus
import com.example.zarur.domain.repository.BookingRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookingRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : BookingRepository {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("zarur_bookings_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    private val bookingsState = MutableStateFlow<List<BookingItem>>(emptyList())

    init {
        loadBookingsFromPrefs()
    }

    private fun loadBookingsFromPrefs() {
        val json = prefs.getString("user_bookings", null)
        if (json.isNullOrEmpty()) {
            val initial = createDefaultBookings()
            saveBookingsToPrefs(initial)
        } else {
            try {
                val type = object : TypeToken<List<BookingItem>>() {}.type
                val list: List<BookingItem> = gson.fromJson(json, type)
                bookingsState.value = list.ifEmpty { createDefaultBookings() }
            } catch (e: Exception) {
                val initial = createDefaultBookings()
                saveBookingsToPrefs(initial)
            }
        }
    }

    private fun saveBookingsToPrefs(list: List<BookingItem>) {
        val json = gson.toJson(list)
        prefs.edit().putString("user_bookings", json).apply()
        bookingsState.value = list
    }

    private fun createDefaultBookings(): List<BookingItem> {
        return listOf(
            BookingItem(
                id = "b1",
                propertyId = "1",
                propertyName = "La Grand Maison Villa",
                propertyImage = "",
                location = "Tashkent, Yakkasaray",
                price = 1200000.0,
                formattedPrice = "1 200 000 uzs",
                dates = "Dec 28, 2024 - Jan 02, 2025",
                bookingCode = "#ZR-88291",
                status = BookingStatus.ACTIVE,
                timestamp = System.currentTimeMillis() - 1000 * 60 * 60 * 2
            ),
            BookingItem(
                id = "b2",
                propertyId = "2",
                propertyName = "iPhone 15 Pro Max 256GB",
                propertyImage = "",
                location = "Official Apple Store",
                price = 14500000.0,
                formattedPrice = "14 500 000 uzs",
                dates = "Dec 26, 2024 • In Delivery",
                bookingCode = "#ZR-99412",
                status = BookingStatus.ACTIVE,
                timestamp = System.currentTimeMillis() - 1000 * 60 * 60 * 24
            ),
            BookingItem(
                id = "b3",
                propertyId = "3",
                propertyName = "Modern Luxury Penthouse",
                propertyImage = "",
                location = "Tashkent, Mirabad",
                price = 2500000.0,
                formattedPrice = "2 500 000 uzs",
                dates = "Nov 10, 2024 - Nov 14, 2024",
                bookingCode = "#ZR-54210",
                status = BookingStatus.COMPLETED,
                timestamp = System.currentTimeMillis() - 1000 * 60 * 60 * 24 * 30
            ),
            BookingItem(
                id = "b4",
                propertyId = "4",
                propertyName = "MacBook Pro M3 Max",
                propertyImage = "",
                location = "iSpace Tashkent",
                price = 32000000.0,
                formattedPrice = "32 000 000 uzs",
                dates = "Delivered Nov 02, 2024",
                bookingCode = "#ZR-33109",
                status = BookingStatus.COMPLETED,
                timestamp = System.currentTimeMillis() - 1000 * 60 * 60 * 24 * 45
            ),
            BookingItem(
                id = "b5",
                propertyId = "5",
                propertyName = "Presidential Suite Tashkent",
                propertyImage = "",
                location = "Tashkent, Chilanzar",
                price = 1800000.0,
                formattedPrice = "1 800 000 uzs",
                dates = "Oct 05, 2024 - Oct 08, 2024",
                bookingCode = "#ZR-11048",
                status = BookingStatus.CANCELLED,
                timestamp = System.currentTimeMillis() - 1000 * 60 * 60 * 24 * 60
            )
        )
    }

    override fun getBookings(): Flow<List<BookingItem>> = bookingsState

    override suspend fun addBooking(booking: BookingItem) {
        val updated = bookingsState.value.toMutableList().apply {
            add(0, booking)
        }
        saveBookingsToPrefs(updated)
    }

    override suspend fun cancelBooking(bookingId: String): Boolean {
        val current = bookingsState.value.toMutableList()
        val index = current.indexOfFirst { it.id == bookingId }
        if (index != -1) {
            val old = current[index]
            current[index] = old.copy(status = BookingStatus.CANCELLED)
            saveBookingsToPrefs(current)
            return true
        }
        return false
    }
}
