package com.example.zarur.data.repository

import android.content.Context
import com.example.zarur.data.local.ProductDao
import com.example.zarur.data.local.ProductEntity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import io.mockk.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ProductRepositoryImplTest {

    private val context: Context = mockk(relaxed = true)
    private val productDao: ProductDao = mockk(relaxed = true)
    private val firestore: FirebaseFirestore = mockk(relaxed = true)
    private val firebaseStorage: FirebaseStorage = mockk(relaxed = true)

    private lateinit var repository: ProductRepositoryImpl

    private val entity = ProductEntity(
        id = "p1",
        name = "iPhone 15",
        price = 1000.0,
        currency = "usd",
        discount = 0,
        rating = 4.9f,
        reviewCount = 10,
        installment = null,
        imageUrl = "http://img.jpg",
        imagesCsv = "http://img.jpg",
        category = "Electronics",
        location = "Tashkent",
        description = "Desc",
        sellerId = "s1",
        isFavorite = true,
        isInCart = true,
        isSold = false,
        timestamp = 1000L
    )

    @Before
    fun setUp() {
        repository = ProductRepositoryImpl(context, productDao, firestore, firebaseStorage)
    }

    @Test
    fun testGetProducts_all() = runTest {
        every { productDao.getAllProducts() } returns flowOf(listOf(entity))

        val products = repository.getProducts("All").first()
        assertEquals(1, products.size)
        assertEquals("iPhone 15", products[0].name)
    }

    @Test
    fun testGetProducts_byCategory() = runTest {
        every { productDao.getProductsByCategory("Electronics") } returns flowOf(listOf(entity))

        val products = repository.getProducts("Electronics").first()
        assertEquals(1, products.size)
        assertEquals("Electronics", products[0].category)
    }

    @Test
    fun testSearchProducts() = runTest {
        every { productDao.searchProducts("%iphone%") } returns flowOf(listOf(entity))

        val products = repository.searchProducts("iphone").first()
        assertEquals(1, products.size)
        assertEquals("p1", products[0].id)
    }

    @Test
    fun testGetProductById() = runTest {
        every { productDao.getProductById("p1") } returns flowOf(entity)

        val product = repository.getProductById("p1").first()
        assertNotNull(product)
        assertEquals("iPhone 15", product?.name)
    }

    @Test
    fun testGetCartProducts() = runTest {
        every { productDao.getCartProducts() } returns flowOf(listOf(entity))

        val products = repository.getCartProducts().first()
        assertEquals(1, products.size)
        assertTrue(products[0].isInCart)
    }

    @Test
    fun testToggleCart() = runTest {
        coEvery { productDao.updateCart("p1", false) } returns 1

        repository.toggleCart("p1", false)

        coVerify { productDao.updateCart("p1", false) }
    }

    @Test
    fun testToggleFavorite() = runTest {
        coEvery { productDao.updateFavorite("p1", true) } returns 1

        repository.toggleFavorite("p1", true)

        coVerify { productDao.updateFavorite("p1", true) }
    }
}
