package com.example.zarur.domain.usecase

import com.example.zarur.data.local.AppPreferences
import com.example.zarur.domain.model.*
import com.example.zarur.domain.repository.*
import io.mockk.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class UseCasesTest {

    private val productRepository: ProductRepository = mockk(relaxed = true)
    private val authRepository: AuthRepository = mockk(relaxed = true)
    private val userRepository: UserRepository = mockk(relaxed = true)
    private val languageRepository: LanguageRepository = mockk(relaxed = true)
    private val chatRepository: ChatRepository = mockk(relaxed = true)
    private val themeRepository: ThemeRepository = mockk(relaxed = true)
    private val pinRepository: PinRepository = mockk(relaxed = true)
    private val appPreferences: AppPreferences = mockk(relaxed = true)

    @Before
    fun setUp() {
        clearAllMocks()
    }

    @Test
    fun testAddReviewUseCase_success() = runTest {
        every { authRepository.getCurrentUserId() } returns "user1"
        every { userRepository.getUserProfile("user1") } returns flowOf(
            Resource.Success(User(id = "user1", fullName = "John Doe", avatarUrl = "avatar.jpg"))
        )
        every { productRepository.addReview(any()) } returns flowOf(
            Resource.Loading,
            Resource.Success(Unit)
        )

        val useCase = AddReviewUseCase(productRepository, authRepository, userRepository)
        val emissions = useCase("p1", 5f, "Great product!").toList()

        assertEquals(3, emissions.size)
        assertTrue(emissions[0] is Resource.Loading)
        assertTrue(emissions[1] is Resource.Loading)
        assertTrue(emissions[2] is Resource.Success)
    }

    @Test
    fun testAddReviewUseCase_noUser() = runTest {
        every { authRepository.getCurrentUserId() } returns null
        every { productRepository.addReview(any()) } returns flowOf(Resource.Success(Unit))

        val useCase = AddReviewUseCase(productRepository, authRepository, userRepository)
        val emissions = useCase("p1", 4f, "Good").toList()

        assertTrue(emissions[0] is Resource.Loading)
        assertTrue(emissions[1] is Resource.Success)
    }

    @Test
    fun testGetCartProductsUseCase() = runTest {
        val products = listOf(Product(id = "1", name = "P1", price = 10.0, category = "Cat", imageUrl = "img"))
        every { productRepository.getCartProducts() } returns flowOf(products)

        val useCase = GetCartProductsUseCase(productRepository)
        val result = useCase().first()

        assertEquals(1, result.size)
        assertEquals("P1", result[0].name)
    }

    @Test
    fun testGetFavoritesUseCase() = runTest {
        val p1 = Product(id = "1", name = "P1", price = 10.0, category = "Cat", imageUrl = "img", isFavorite = true)
        val p2 = Product(id = "2", name = "P2", price = 20.0, category = "Cat", imageUrl = "img", isFavorite = false)
        every { productRepository.getProducts() } returns flowOf(listOf(p1, p2))

        val useCase = GetFavoritesUseCase(productRepository)
        val result = useCase().first()

        assertEquals(1, result.size)
        assertEquals("1", result[0].id)
    }

    @Test
    fun testGetLanguageUseCase() = runTest {
        every { languageRepository.getLanguage() } returns flowOf("uz")

        val useCase = GetLanguageUseCase(languageRepository)
        val lang = useCase().first()

        assertEquals("uz", lang)
    }

    @Test
    fun testGetMessagesUseCase() = runTest {
        val msgs = listOf(ChatMessage(id = "m1", text = "Hi"))
        every { chatRepository.getMessages("c1") } returns flowOf(msgs)

        val useCase = GetMessagesUseCase(chatRepository)
        val result = useCase("c1").first()

        assertEquals(1, result.size)
        assertEquals("Hi", result[0].text)
    }

    @Test
    fun testGetProductReviewsUseCase() = runTest {
        val reviews = listOf(Review(id = "r1", reviewText = "Nice"))
        every { productRepository.getReviewsForProduct("p1") } returns flowOf(reviews)

        val useCase = GetProductReviewsUseCase(productRepository)
        val result = useCase("p1").first()

        assertEquals(1, result.size)
        assertEquals("Nice", result[0].reviewText)
    }

    @Test
    fun testGetProductUseCase() = runTest {
        val product = Product(id = "p1", name = "Phone", price = 100.0, category = "Cat", imageUrl = "img")
        every { productRepository.getProductById("p1") } returns flowOf(product)

        val useCase = GetProductUseCase(productRepository)
        val result = useCase("p1").first()

        assertNotNull(result)
        assertEquals("Phone", result?.name)
    }

    @Test
    fun testGetPropertiesUseCase() = runTest {
        val products = listOf(Product(id = "1", name = "Prop", price = 10.0, category = "Cat", imageUrl = "img"))
        every { productRepository.getProducts("Cat") } returns flowOf(products)

        val useCase = GetPropertiesUseCase(productRepository)
        val result = useCase("Cat").first()

        assertEquals(1, result.size)
        verify { productRepository.getProducts("Cat") }
    }

    @Test
    fun testGetSellerProductsUseCase() = runTest {
        every { authRepository.getCurrentUserId() } returns "s1"
        val products = listOf(Product(id = "1", name = "P1", price = 10.0, category = "Cat", imageUrl = "img", sellerId = "s1"))
        every { productRepository.getSellerProducts("s1") } returns flowOf(products)

        val useCase = GetSellerProductsUseCase(productRepository, authRepository)
        val result = useCase().first()

        assertEquals(1, result.size)

        // Test with explicit sellerId parameter
        every { productRepository.getSellerProducts("s2") } returns flowOf(products)
        val result2 = useCase("s2").first()
        assertEquals(1, result2.size)

        // Test with null currentUserId and no sellerId
        every { authRepository.getCurrentUserId() } returns null
        val result3 = useCase().first()
        assertTrue(result3.isEmpty())
    }

    @Test
    fun testGetThemeUseCase() = runTest {
        every { themeRepository.isDarkMode() } returns flowOf(true)

        val useCase = GetThemeUseCase(themeRepository)
        val isDark = useCase().first()

        assertTrue(isDark)
    }

    @Test
    fun testGetUserProfileUseCase() = runTest {
        every { authRepository.getCurrentUserId() } returns "u1"
        every { userRepository.getUserProfile("u1") } returns flowOf(Resource.Success(User(id = "u1", fullName = "Alice")))

        val useCase = GetUserProfileUseCase(userRepository, authRepository)
        val res = useCase().first()

        assertTrue(res is Resource.Success)
        assertEquals("Alice", (res as Resource.Success).data?.fullName)

        // Test unauthenticated
        every { authRepository.getCurrentUserId() } returns null
        val unauthRes = useCase().first()
        assertTrue(unauthRes is Resource.Error)
    }

    @Test
    fun testIsLoggedInUseCase() = runTest {
        every { appPreferences.isLoggedIn } returns true

        val useCase = IsLoggedInUseCase(appPreferences)
        assertTrue(useCase())
    }

    @Test
    fun testIsPinSetUseCase() = runTest {
        coEvery { pinRepository.isPinSet() } returns true

        val useCase = IsPinSetUseCase(pinRepository)
        assertTrue(useCase())
    }

    @Test
    fun testSavePinUseCase() = runTest {
        coEvery { pinRepository.savePin("1234") } returns Unit

        val useCase = SavePinUseCase(pinRepository)
        useCase("1234")

        coVerify { pinRepository.savePin("1234") }
    }

    @Test
    fun testSearchPropertiesUseCase() = runTest {
        val products = listOf(Product(id = "1", name = "Search Result", price = 10.0, category = "Cat", imageUrl = "img"))
        every { productRepository.searchProducts("iphone") } returns flowOf(products)

        val useCase = SearchPropertiesUseCase(productRepository)
        val result = useCase("iphone").first()

        assertEquals(1, result.size)
        assertEquals("Search Result", result[0].name)
    }

    @Test
    fun testSendMessageUseCase() = runTest {
        val msg = ChatMessage(id = "m1", text = "Hello")
        coEvery { chatRepository.sendMessage("c1", msg) } returns Unit

        val useCase = SendMessageUseCase(chatRepository)
        useCase("c1", msg)

        coVerify { chatRepository.sendMessage("c1", msg) }
    }

    @Test
    fun testSetLanguageUseCase() = runTest {
        coEvery { languageRepository.setLanguage("en") } returns Unit

        val useCase = SetLanguageUseCase(languageRepository)
        useCase("en")

        coVerify { languageRepository.setLanguage("en") }
    }

    @Test
    fun testSetThemeUseCase() = runTest {
        coEvery { themeRepository.setDarkMode(true) } returns Unit

        val useCase = SetThemeUseCase(themeRepository)
        useCase(true)

        coVerify { themeRepository.setDarkMode(true) }
    }

    @Test
    fun testSignInUseCase() = runTest {
        val user = User(id = "u1", email = "test@test.com")
        every { authRepository.signIn("test@test.com", "pass") } returns flowOf(Resource.Success(user))

        val useCase = SignInUseCase(authRepository)
        val result = useCase("test@test.com", "pass").first()

        assertTrue(result is Resource.Success)
        assertEquals("u1", (result as Resource.Success).data.id)
    }

    @Test
    fun testSignInWithGoogleUseCase() = runTest {
        val user = User(id = "u1", email = "test@test.com")
        every { authRepository.signInWithGoogle("token") } returns flowOf(Resource.Success(user))

        val useCase = SignInWithGoogleUseCase(authRepository)
        val result = useCase("token").first()

        assertTrue(result is Resource.Success)
        assertEquals("u1", (result as Resource.Success).data.id)
    }

    @Test
    fun testSignOutUseCase() = runTest {
        every { authRepository.signOut() } returns Unit

        val useCase = SignOutUseCase(authRepository)
        useCase()

        verify { authRepository.signOut() }
    }

    @Test
    fun testSignUpUseCase() = runTest {
        val user = User(id = "u1", email = "test@test.com", fullName = "Name")
        every { authRepository.signUp("test@test.com", "pass", "Name") } returns flowOf(Resource.Success(user))

        val useCase = SignUpUseCase(authRepository)
        val result = useCase("test@test.com", "pass", "Name").first()

        assertTrue(result is Resource.Success)
        assertEquals("Name", (result as Resource.Success).data.fullName)
    }

    @Test
    fun testSyncPropertiesUseCase() = runTest {
        coEvery { productRepository.syncProducts() } returns Unit

        val useCase = SyncPropertiesUseCase(productRepository)
        useCase()

        coVerify { productRepository.syncProducts() }
    }

    @Test
    fun testToggleCartUseCase() = runTest {
        coEvery { productRepository.toggleCart("p1", true) } returns Unit

        val useCase = ToggleCartUseCase(productRepository)
        useCase("p1", true)

        coVerify { productRepository.toggleCart("p1", true) }
    }

    @Test
    fun testToggleFavoriteUseCase() = runTest {
        coEvery { productRepository.toggleFavorite("p1", true) } returns Unit

        val useCase = ToggleFavoriteUseCase(productRepository)
        useCase("p1", true)

        coVerify { productRepository.toggleFavorite("p1", true) }
    }

    @Test
    fun testUpdateUserProfileUseCase() = runTest {
        every { authRepository.getCurrentUserId() } returns "u1"
        every { userRepository.updateUserProfile(any()) } returns flowOf(Resource.Success(Unit))

        val useCase = UpdateUserProfileUseCase(userRepository, authRepository)
        val emissions = useCase("Name", "Nick", "email@a.com", "123").toList()

        assertTrue(emissions[0] is Resource.Loading)
        assertTrue(emissions[1] is Resource.Success)

        // Test unauthenticated
        every { authRepository.getCurrentUserId() } returns null
        val unauthEmissions = useCase("Name", "Nick", "email@a.com", "123").toList()
        assertTrue(unauthEmissions[0] is Resource.Error)
    }

    @Test
    fun testUpdateUserProfileUseCase_withAvatarUpload() = runTest {
        every { authRepository.getCurrentUserId() } returns "u1"
        every { userRepository.uploadAvatar("u1", "content://image") } returns flowOf(
            Resource.Success("http://avatar.jpg")
        )
        every { userRepository.updateUserProfile(any()) } returns flowOf(Resource.Success(Unit))

        val useCase = UpdateUserProfileUseCase(userRepository, authRepository)
        val emissions = useCase("Name", "Nick", "email@a.com", "123", imageUri = "content://image").toList()

        assertTrue(emissions[0] is Resource.Loading)
        assertTrue(emissions[1] is Resource.Success)
    }

    @Test
    fun testUpdateUserProfileUseCase_withAvatarUploadError() = runTest {
        every { authRepository.getCurrentUserId() } returns "u1"
        every { userRepository.uploadAvatar("u1", "content://image") } returns flowOf(
            Resource.Error("Upload failed")
        )

        val useCase = UpdateUserProfileUseCase(userRepository, authRepository)
        val emissions = useCase("Name", "Nick", "email@a.com", "123", imageUri = "content://image").toList()

        assertTrue(emissions[0] is Resource.Loading)
        assertTrue(emissions[1] is Resource.Error)
    }

    @Test
    fun testUploadProductUseCase() = runTest {
        every { authRepository.getCurrentUserId() } returns "seller1"
        val createdProduct = Product(id = "p1", name = "Item", price = 50.0, category = "Cat", imageUrl = "img")
        every { productRepository.uploadProduct(any(), any()) } returns flowOf(
            Resource.Success(createdProduct)
        )

        val useCase = UploadProductUseCase(productRepository, authRepository)
        val emissions = useCase("Item", "Cat", 50.0, "Desc", "uri").toList()

        assertTrue(emissions[0] is Resource.Success)
        assertEquals("Item", (emissions[0] as Resource.Success).data.name)
    }

    @Test
    fun testValidatePinUseCase() = runTest {
        coEvery { pinRepository.validatePin("1234") } returns true

        val useCase = ValidatePinUseCase(pinRepository)
        assertTrue(useCase("1234"))
    }
}
