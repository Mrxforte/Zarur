package com.example.zarur.presentation

import com.example.zarur.domain.model.Product
import com.example.zarur.domain.usecase.GetPropertiesUseCase
import com.example.zarur.domain.usecase.GetUserProfileUseCase
import com.example.zarur.domain.usecase.IsLoggedInUseCase
import com.example.zarur.domain.usecase.SearchPropertiesUseCase
import com.example.zarur.domain.usecase.SyncPropertiesUseCase
import com.example.zarur.domain.usecase.ToggleCartUseCase
import com.example.zarur.domain.usecase.ToggleFavoriteUseCase
import com.example.zarur.presentation.explore.ExploreViewModel
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
class ExploreViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private val getPropertiesUseCase: GetPropertiesUseCase = mockk()
    private val syncPropertiesUseCase: SyncPropertiesUseCase = mockk()
    private val searchPropertiesUseCase: SearchPropertiesUseCase = mockk()
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase = mockk()
    private val toggleCartUseCase: ToggleCartUseCase = mockk(relaxed = true)
    private val isLoggedInUseCase: IsLoggedInUseCase = mockk(relaxed = true)
    private val getUserProfileUseCase: GetUserProfileUseCase = mockk(relaxed = true)

    private lateinit var viewModel: ExploreViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        coEvery { syncPropertiesUseCase() } returns Unit
        every { getPropertiesUseCase(any()) } returns flowOf(emptyList())
        every { searchPropertiesUseCase(any()) } returns flowOf(emptyList())
        every { getUserProfileUseCase() } returns flowOf(com.example.zarur.domain.model.Resource.Success(null))

        viewModel = ExploreViewModel(
            getPropertiesUseCase,
            syncPropertiesUseCase,
            searchPropertiesUseCase,
            toggleFavoriteUseCase,
            toggleCartUseCase,
            isLoggedInUseCase,
            getUserProfileUseCase
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testInit_syncs() {
        coVerify { syncPropertiesUseCase() }
    }

    @Test
    fun testSetCategory_andSearch() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.properties.collect()
        }

        val products = listOf(Product(id = "p1", name = "Car", price = 100.0, category = "Auto", imageUrl = "img"))
        every { getPropertiesUseCase("Auto") } returns flowOf(products)

        viewModel.setCategory("Auto")
        assertEquals(products, viewModel.properties.value)

        val searchProducts = listOf(Product(id = "p2", name = "Search Phone", price = 200.0, category = "Electronics", imageUrl = "img"))
        every { searchPropertiesUseCase("Phone") } returns flowOf(searchProducts)

        viewModel.search("Phone")
        assertEquals(searchProducts, viewModel.properties.value)
    }

    @Test
    fun testToggleFavorite() = runTest {
        val product = Product(id = "p1", name = "Car", price = 100.0, category = "Auto", imageUrl = "img", isFavorite = false)
        coEvery { toggleFavoriteUseCase("p1", true) } returns Unit

        viewModel.toggleFavorite(product)

        coVerify { toggleFavoriteUseCase("p1", true) }
    }
}
