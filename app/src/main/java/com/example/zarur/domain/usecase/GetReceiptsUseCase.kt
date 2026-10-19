package com.example.zarur.domain.usecase

import com.example.zarur.domain.model.Receipt
import com.example.zarur.domain.repository.ReceiptRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetReceiptsUseCase @Inject constructor(
    private val repository: ReceiptRepository
) {
    operator fun invoke(): Flow<List<Receipt>> = repository.getReceipts()
}
