package com.example.zarur.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY timestamp DESC")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE category = :category ORDER BY timestamp DESC")
    fun getProductsByCategory(category: String): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE name LIKE :query")
    fun searchProducts(query: String): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id")
    fun getProductById(id: String): Flow<ProductEntity?>

    @Query("SELECT * FROM products WHERE isInCart = 1 ORDER BY timestamp DESC")
    fun getCartProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE sellerId = :sellerId ORDER BY timestamp DESC")
    fun getProductsBySeller(sellerId: String): Flow<List<ProductEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertProducts(products: List<ProductEntity>)

    @Query("DELETE FROM products")
    fun clearAll(): Int

    @Update
    fun updateProduct(product: ProductEntity)

    @Query("UPDATE products SET isFavorite = :isFavorite WHERE id = :id")
    fun updateFavorite(id: String, isFavorite: Boolean): Int

    @Query("UPDATE products SET isInCart = :isInCart WHERE id = :id")
    fun updateCart(id: String, isInCart: Boolean): Int
}
