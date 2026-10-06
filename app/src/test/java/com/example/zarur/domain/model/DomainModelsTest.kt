package com.example.zarur.domain.model

import com.example.zarur.data.local.ProductEntity
import com.example.zarur.presentation.common.UiState
import org.junit.Assert.*
import org.junit.Test

class DomainModelsTest {

    @Test
    fun testProduct_getAllImages() {
        val productWithImages = Product(
            id = "1",
            name = "Test",
            price = 100.0,
            imageUrl = "main.jpg",
            images = listOf("img1.jpg", "img2.jpg"),
            category = "Electronics"
        )
        assertEquals(listOf("img1.jpg", "img2.jpg"), productWithImages.getAllImages())

        val productWithoutImages = Product(
            id = "2",
            name = "Test 2",
            price = 200.0,
            imageUrl = "main.jpg",
            images = emptyList(),
            category = "Fashion"
        )
        assertEquals(listOf("main.jpg"), productWithoutImages.getAllImages())
    }

    @Test
    fun testProductEntity_toDomainAndFromDomain() {
        val product = Product(
            id = "p1",
            name = "Phone",
            price = 999.0,
            currency = "usd",
            discount = 10,
            rating = 4.5f,
            reviewCount = 12,
            installment = "100/mo",
            imageUrl = "http://img.png",
            images = listOf("http://img1.png", "http://img2.png"),
            category = "Electronics",
            location = "Tashkent",
            description = "Desc",
            sellerId = "s1",
            isFavorite = true,
            isInCart = false,
            isSold = false,
            timestamp = 1000L
        )

        val entity = ProductEntity.fromDomain(product)
        assertEquals("p1", entity.id)
        assertEquals("http://img1.png,http://img2.png", entity.imagesCsv)

        val mappedProduct = entity.toDomain()
        assertEquals(product.id, mappedProduct.id)
        assertEquals(product.name, mappedProduct.name)
        assertEquals(product.price, mappedProduct.price, 0.001)
        assertEquals(product.images, mappedProduct.images)
        assertEquals(product.isFavorite, mappedProduct.isFavorite)
    }

    @Test
    fun testProductCategory_allCategories() {
        val categories = ProductCategory.all
        assertEquals(7, categories.size)
        assertTrue(categories.contains(ProductCategory.Auto))
        assertTrue(categories.contains(ProductCategory.RealEstate))
        assertTrue(categories.contains(ProductCategory.Electronics))
        assertTrue(categories.contains(ProductCategory.Fashion))
        assertTrue(categories.contains(ProductCategory.Jobs))
        assertTrue(categories.contains(ProductCategory.Services))
        assertTrue(categories.contains(ProductCategory.Home))

        assertEquals("auto", ProductCategory.Auto.id)
        assertEquals("cat_auto", ProductCategory.Auto.displayNameRes)
    }

    @Test
    fun testUser_defaults() {
        val user = User()
        assertEquals("", user.id)
        assertEquals("", user.fullName)
        assertEquals("", user.email)
        assertNull(user.avatarUrl)
    }

    @Test
    fun testChatMessage_defaults() {
        val msg = ChatMessage(id = "m1", text = "Hello", time = "12:00")
        assertEquals("m1", msg.id)
        assertEquals("Hello", msg.text)
        assertTrue(msg.isSent)
        assertFalse(msg.isProperty)
    }

    @Test
    fun testOnboardingPage() {
        val page = OnboardingPage("Title", "Desc", 123)
        assertEquals("Title", page.title)
        assertEquals("Desc", page.description)
        assertEquals(123, page.imageRes)
    }

    @Test
    fun testReview() {
        val review = Review(id = "r1", productId = "p1", rating = 4.8f, reviewText = "Great")
        assertEquals("r1", review.id)
        assertEquals("p1", review.productId)
        assertEquals(4.8f, review.rating, 0.01f)
        assertEquals("Great", review.reviewText)
        assertEquals("Anonymous", review.userName)
    }

    @Test
    fun testResourceSealedClass() {
        val loading = Resource.Loading
        val success = Resource.Success("Data")
        val error = Resource.Error("Failed")

        assertTrue(loading is Resource.Loading)
        assertEquals("Data", success.data)
        assertEquals("Failed", error.message)
    }

    @Test
    fun testUiStateSealedClass() {
        val loading = UiState.Loading
        val success = UiState.Success("Data")
        val error = UiState.Error("Err")

        assertTrue(loading is UiState.Loading)
        assertEquals("Data", success.data)
        assertEquals("Err", error.message)
    }
}
