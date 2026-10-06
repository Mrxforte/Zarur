package com.example.zarur.presentation

import com.example.zarur.domain.model.Product
import com.example.zarur.domain.model.Resource
import com.example.zarur.domain.model.Review
import com.example.zarur.domain.model.User
import com.example.zarur.domain.usecase.*
import com.example.zarur.presentation.details.ProductDetailUiEvent
import com.example.zarur.presentation.details.ProductDetailViewModel
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
class ProductDetailViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private val getProductUseCase: GetProductUseCase = mockk()
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase = mockk()
    private val toggleCartUseCase: ToggleCartUseCase = mockk()
    private val isLoggedInUseCase: IsLoggedInUseCase = mockk()
    private val getProductReviewsUseCase: GetProductReviewsUseCase = mockk()
    private val addReviewUseCase: AddReviewUseCase = mockk()
    private val getPropertiesUseCase: GetPropertiesUseCase = mockk()
    private val getUserProfileUseCase: GetUserProfileUseCase = mockk()

    private lateinit var viewModel: ProductDetailViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        every { getUserProfileUseCase() } returns flowOf(Resource.Success(User(id = "u1", fullName = "John")))
        every { getPropertiesUseCase(any()) } returns flowOf(emptyList())

        viewModel = ProductDetailViewModel(
            getProductUseCase,
            toggleFavoriteUseCase,
            toggleCartUseCase,
            isLoggedInUseCase,
            getProductReviewsUseCase,
            addReviewUseCase,
            getPropertiesUseCase,
            getUserProfileUseCase
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testLoadProduct_andStreams() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.product.collect() }
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.reviews.collect() }

        val product = Product(id = "p1", name = "Test Phone", price = 100.0, category = "Electronics", imageUrl = "img")
        val reviews = listOf(Review(id = "r1", reviewText = "Good"))

        every { getProductUseCase("p1") } returns flowOf(product)
        every { getProductReviewsUseCase("p1") } returns flowOf(reviews)

        viewModel.loadProduct("p1")

        assertEquals(product, viewModel.product.value)
        assertEquals(reviews, viewModel.reviews.value)
    }

    @Test
    fun testToggleFavorite_loggedIn() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.product.collect() }

        val product = Product(id = "p1", name = "Phone", price = 100.0, category = "Electronics", imageUrl = "img", isFavorite = false)
        every { getProductUseCase("p1") } returns flowOf(product)
        every { getProductReviewsUseCase("p1") } returns flowOf(emptyList())
        every { isLoggedInUseCase() } returns true
        coEvery { toggleFavoriteUseCase("p1", true) } returns Unit

        viewModel.loadProduct("p1")
        viewModel.toggleFavorite()

        coVerify { toggleFavoriteUseCase("p1", true) }
    }

    @Test
    fun testToggleFavorite_loggedOut() = runTest {
        val events = mutableListOf<ProductDetailUiEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiEvent.collect { events.add(it) } }

        val product = Product(id = "p1", name = "Phone", price = 100.0, category = "Electronics", imageUrl = "img", isFavorite = false)
        every { getProductUseCase("p1") } returns flowOf(product)
        every { getProductReviewsUseCase("p1") } returns flowOf(emptyList())
        every { isLoggedInUseCase() } returns false

        viewModel.loadProduct("p1")
        viewModel.toggleFavorite()

        assertEquals(1, events.size)
        assertTrue(events[0] is ProductDetailUiEvent.NavigateToAuth)
    }

    @Test
    fun testAddToCart_loggedIn() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.product.collect() }
        val events = mutableListOf<ProductDetailUiEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiEvent.collect { events.add(it) } }

        val product = Product(id = "p1", name = "Phone", price = 100.0, category = "Electronics", imageUrl = "img")
        every { getProductUseCase("p1") } returns flowOf(product)
        every { getProductReviewsUseCase("p1") } returns flowOf(emptyList())
        every { isLoggedInUseCase() } returns true
        coEvery { toggleCartUseCase("p1", true) } returns Unit

        viewModel.loadProduct("p1")
        viewModel.addToCart()

        coVerify { toggleCartUseCase("p1", true) }
        assertEquals(1, events.size)
        assertTrue(events[0] is ProductDetailUiEvent.ShowAddToCartSuccess)
    }

    @Test
    fun testBuyNow_andChat() = runTest {
        val events = mutableListOf<ProductDetailUiEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiEvent.collect { events.add(it) } }

        every { isLoggedInUseCase() } returns true

        viewModel.buyNow()
        viewModel.chat()

        assertEquals(2, events.size)
        assertTrue(events[0] is ProductDetailUiEvent.NavigateToBooking)
        assertTrue(events[1] is ProductDetailUiEvent.NavigateToChat)
    }

    @Test
    fun testSubmitReview_success() = runTest {
        val events = mutableListOf<ProductDetailUiEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiEvent.collect { events.add(it) } }

        val product = Product(id = "p1", name = "Phone", price = 100.0, category = "Electronics", imageUrl = "img")
        every { getProductUseCase("p1") } returns flowOf(product)
        every { getProductReviewsUseCase("p1") } returns flowOf(emptyList())
        every { isLoggedInUseCase() } returns true
        every { addReviewUseCase("p1", 5f, "Nice") } returns flowOf(Resource.Success(Unit))

        viewModel.loadProduct("p1")
        viewModel.submitReview("Nice", 5f)

        assertEquals(1, events.size)
        assertTrue(events[0] is ProductDetailUiEvent.ShowReviewSuccess)
    }
}
