package com.example.zarur.presentation

import com.example.zarur.domain.model.Product
import com.example.zarur.domain.usecase.GetCartProductsUseCase
import com.example.zarur.domain.usecase.GetFavoritesUseCase
import com.example.zarur.domain.usecase.ToggleCartUseCase
import com.example.zarur.domain.usecase.ToggleFavoriteUseCase
import com.example.zarur.presentation.favorites.FavoritesViewModel
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
class FavoritesViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private val getFavoritesUseCase: GetFavoritesUseCase = mockk()
    private val getCartProductsUseCase: GetCartProductsUseCase = mockk()
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase = mockk()
    private val toggleCartUseCase: ToggleCartUseCase = mockk()

    private lateinit var viewModel: FavoritesViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        val favProducts = listOf(Product(id = "p1", name = "Fav", price = 100.0, category = "Cat", imageUrl = "img", isFavorite = true))
        val cartProducts = listOf(Product(id = "p2", name = "Cart", price = 200.0, category = "Cat", imageUrl = "img", isInCart = true))

        every { getFavoritesUseCase() } returns flowOf(favProducts)
        every { getCartProductsUseCase() } returns flowOf(cartProducts)

        viewModel = FavoritesViewModel(
            getFavoritesUseCase,
            getCartProductsUseCase,
            toggleFavoriteUseCase,
            toggleCartUseCase
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testInitialState() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.favoriteProducts.collect()
        }
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.cartProducts.collect()
        }

        assertEquals(1, viewModel.favoriteProducts.value.size)
        assertEquals("Fav", viewModel.favoriteProducts.value[0].name)

        assertEquals(1, viewModel.cartProducts.value.size)
        assertEquals("Cart", viewModel.cartProducts.value[0].name)
    }

    @Test
    fun testToggleFavorite_andRemoveFromCart() = runTest {
        val product = Product(id = "p1", name = "Fav", price = 100.0, category = "Cat", imageUrl = "img", isFavorite = true)
        coEvery { toggleFavoriteUseCase("p1", false) } returns Unit
        coEvery { toggleCartUseCase("p1", false) } returns Unit

        viewModel.toggleFavorite(product)
        coVerify { toggleFavoriteUseCase("p1", false) }

        viewModel.removeFromCart(product)
        coVerify { toggleCartUseCase("p1", false) }
    }
}
