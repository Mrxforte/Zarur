package com.example.zarur.presentation

import com.example.zarur.domain.model.Product
import com.example.zarur.domain.model.Resource
import com.example.zarur.domain.usecase.GetSellerProductsUseCase
import com.example.zarur.domain.usecase.IsLoggedInUseCase
import com.example.zarur.domain.usecase.UploadProductUseCase
import com.example.zarur.presentation.common.UiState
import com.example.zarur.presentation.seller.SellerViewModel
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SellerViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private val uploadProductUseCase: UploadProductUseCase = mockk()
    private val getSellerProductsUseCase: GetSellerProductsUseCase = mockk()
    private val isLoggedInUseCase: IsLoggedInUseCase = mockk()

    private lateinit var viewModel: SellerViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        val sellerProducts = listOf(Product(id = "p1", name = "P1", price = 100.0, category = "Cat", imageUrl = "img"))
        every { getSellerProductsUseCase() } returns flowOf(sellerProducts)
        every { isLoggedInUseCase() } returns true

        viewModel = SellerViewModel(uploadProductUseCase, getSellerProductsUseCase, isLoggedInUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testSellerProducts() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.sellerProducts.collect() }
        assertEquals(1, viewModel.sellerProducts.value.size)
    }

    @Test
    fun testUploadProduct_andReset() = runTest {
        val createdProduct = Product(id = "p2", name = "New Prod", price = 50.0, category = "Electronics", imageUrl = "img")
        viewModel.setSelectedPhoto("file://photo.jpg")

        every {
            uploadProductUseCase("New Prod", "Electronics", 50.0, "Desc", "file://photo.jpg")
        } returns flowOf(Resource.Loading, Resource.Success(createdProduct))

        viewModel.uploadProduct("New Prod", "Electronics", 50.0, "Desc")

        val state = viewModel.uploadState.value
        assertTrue(state is UiState.Success)
        assertEquals("New Prod", (state as UiState.Success).data.name)

        viewModel.resetUploadState()
        assertNull(viewModel.uploadState.value)
    }

    @Test
    fun testUploadProduct_whenNotLoggedIn_emitsAuthRequired() = runTest {
        every { isLoggedInUseCase() } returns false

        viewModel.uploadProduct("New Prod", "Electronics", 50.0, "Desc")

        val state = viewModel.uploadState.value
        assertTrue(state is UiState.Error)
        assertEquals("AUTH_REQUIRED", (state as UiState.Error).message)
    }
}
