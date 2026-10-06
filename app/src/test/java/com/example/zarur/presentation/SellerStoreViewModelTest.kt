package com.example.zarur.presentation

import com.example.zarur.domain.model.Product
import com.example.zarur.domain.usecase.GetPropertiesUseCase
import com.example.zarur.domain.usecase.GetSellerProductsUseCase
import com.example.zarur.domain.usecase.ToggleCartUseCase
import com.example.zarur.presentation.seller.SellerStoreViewModel
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
class SellerStoreViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private val getSellerProductsUseCase: GetSellerProductsUseCase = mockk()
    private val getPropertiesUseCase: GetPropertiesUseCase = mockk()
    private val toggleCartUseCase: ToggleCartUseCase = mockk()

    private lateinit var viewModel: SellerStoreViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        viewModel = SellerStoreViewModel(
            getSellerProductsUseCase,
            getPropertiesUseCase,
            toggleCartUseCase
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testLoadSeller_andAddToCart() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.sellerProducts.collect() }
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.relatedProducts.collect() }

        val sellerProds = listOf(Product(id = "p1", name = "Seller Item", price = 10.0, category = "Cat", imageUrl = "img", sellerId = "s1"))
        val otherProds = listOf(Product(id = "p2", name = "Other Item", price = 20.0, category = "Cat", imageUrl = "img", sellerId = "s2"))

        every { getSellerProductsUseCase("s1") } returns flowOf(sellerProds)
        every { getPropertiesUseCase() } returns flowOf(otherProds)
        coEvery { toggleCartUseCase("p1", true) } returns Unit

        viewModel.loadSeller("s1")

        assertEquals(sellerProds, viewModel.sellerProducts.value)
        assertEquals(otherProds, viewModel.relatedProducts.value)

        viewModel.addToCart(sellerProds[0])
        coVerify { toggleCartUseCase("p1", true) }
    }
}
