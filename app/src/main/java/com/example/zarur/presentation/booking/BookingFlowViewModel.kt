package com.example.zarur.presentation.booking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zarur.domain.model.BookingItem
import com.example.zarur.domain.model.BookingStatus
import com.example.zarur.domain.model.Product
import com.example.zarur.domain.model.Receipt
import com.example.zarur.domain.repository.BookingRepository
import com.example.zarur.domain.repository.ReceiptRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class BookingFlowData(
    val productId: String = "",
    val productName: String = "",
    val productImage: String = "",
    val productLocation: String = "",
    val productCategory: String = "Real Estate",
    val price: Double = 0.0,
    val formattedPrice: String = "",
    val checkInDate: String = "",
    val checkOutDate: String = "",
    val dates: String = "",
    val fullName: String = "",
    val nickname: String = "",
    val email: String = "",
    val phone: String = "",
    val dob: String = "",
    val paymentMethod: String = "Uzcard / Humo",
    val paymentIconRes: Int = 0
)

@HiltViewModel
class BookingFlowViewModel @Inject constructor(
    private val bookingRepository: BookingRepository,
    private val receiptRepository: ReceiptRepository
) : ViewModel() {

    private val _bookingData = MutableStateFlow(BookingFlowData())
    val bookingData: StateFlow<BookingFlowData> = _bookingData.asStateFlow()

    private val _latestReceipt = MutableStateFlow<Receipt?>(null)
    val latestReceipt: StateFlow<Receipt?> = _latestReceipt.asStateFlow()

    fun initBooking(product: Product) {
        val priceStr = "%,.0f uzs".format(product.price).replace(",", " ")
        _bookingData.value = BookingFlowData(
            productId = product.id,
            productName = product.name,
            productImage = product.imageUrl,
            productLocation = product.location,
            productCategory = product.category,
            price = product.price,
            formattedPrice = priceStr,
            dates = "Dec 28 - Jan 02 2025"
        )
    }

    fun setDates(checkIn: String, checkOut: String) {
        val dateText = if (checkIn.isNotBlank() && checkOut.isNotBlank()) {
            "$checkIn - $checkOut"
        } else {
            checkIn.ifBlank { checkOut }
        }
        _bookingData.value = _bookingData.value.copy(
            checkInDate = checkIn,
            checkOutDate = checkOut,
            dates = dateText
        )
    }

    fun setContactInfo(
        fullName: String,
        nickname: String,
        email: String,
        phone: String,
        dob: String
    ) {
        _bookingData.value = _bookingData.value.copy(
            fullName = fullName,
            nickname = nickname,
            email = email,
            phone = phone,
            dob = dob
        )
    }

    fun setPaymentMethod(methodName: String, iconRes: Int = 0) {
        _bookingData.value = _bookingData.value.copy(
            paymentMethod = methodName,
            paymentIconRes = iconRes
        )
    }

    fun finalizeBookingAndCreateReceipt(): Receipt {
        val current = _bookingData.value
        val randomNum = (10000..99999).random()
        val transactionId = (5000000000..9999999999).random().toString()
        val bookingId = "b_" + System.currentTimeMillis()

        val pName = current.productName.ifEmpty { "La Grand Maison Villa" }
        val pPrice = if (current.price > 0) current.price else 1200000.0
        val pFormatted = current.formattedPrice.ifEmpty { "1 200 000 uzs" }
        val pLoc = current.productLocation.ifEmpty { "Tashkent" }
        val pDates = current.dates.ifEmpty { "Dec 28, 2024 - Jan 02, 2025" }

        val newBooking = BookingItem(
            id = bookingId,
            propertyId = current.productId.ifEmpty { "1" },
            propertyName = pName,
            propertyImage = current.productImage,
            location = pLoc,
            price = pPrice,
            formattedPrice = pFormatted,
            dates = pDates,
            bookingCode = "#ZR-$randomNum",
            status = BookingStatus.ACTIVE,
            timestamp = System.currentTimeMillis()
        )

        val currentDateStr = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date())

        val newReceipt = Receipt(
            id = "rcpt_" + System.currentTimeMillis(),
            transactionId = transactionId,
            productName = pName,
            productImage = current.productImage,
            priceFormatted = pFormatted,
            category = current.productCategory.ifEmpty { "Real Estate" },
            date = currentDateStr,
            status = "PAID",
            paymentMethod = current.paymentMethod.ifEmpty { "Uzcard / Humo" },
            userName = current.fullName,
            userPhone = current.phone,
            timestamp = System.currentTimeMillis()
        )

        viewModelScope.launch {
            bookingRepository.addBooking(newBooking)
            receiptRepository.addReceipt(newReceipt)
        }

        _latestReceipt.value = newReceipt
        return newReceipt
    }
}
