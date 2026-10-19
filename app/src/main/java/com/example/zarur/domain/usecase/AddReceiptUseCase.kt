package com.example.zarur.domain.usecase

import com.example.zarur.domain.model.Receipt
import com.example.zarur.domain.repository.ReceiptRepository
import javax.inject.Inject

class AddReceiptUseCase @Inject constructor(
    private val repository: ReceiptRepository
) {
    suspend operator fun invoke(receipt: Receipt) = repository.addReceipt(receipt)
}
