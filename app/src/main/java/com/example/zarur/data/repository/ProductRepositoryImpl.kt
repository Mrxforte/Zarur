package com.example.zarur.data.repository

import android.content.Context
import android.net.Uri
import com.example.zarur.data.local.ProductDao
import com.example.zarur.data.local.ProductEntity
import com.example.zarur.domain.model.Product
import com.example.zarur.domain.model.Resource
import com.example.zarur.domain.model.Review
import com.example.zarur.domain.repository.ProductRepository
import com.example.zarur.util.parseFirebaseError
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val productDao: ProductDao,
    private val firestore: FirebaseFirestore,
    private val firebaseStorage: FirebaseStorage
) : ProductRepository {

    override fun getProducts(category: String?): Flow<List<Product>> {
        val mappedCategory = when(category) {
            "Электроника", "Electronics", "Elektronika" -> "Electronics"
            "Транспорт", "Cars", "Transport" -> "Cars"
            "Недвижимость", "Real Estate", "Ko'chmas mulk" -> "Real Estate"
            "Работа", "Jobs", "Ish" -> "Jobs"
            "Услуги", "Services", "Xizmatlar" -> "Services"
            "Бытовая техника", "Home Appliances", "Maishiy texnika" -> "Home Appliances"
            else -> category
        }
        
        val flow = if (mappedCategory == null || mappedCategory == "All" || mappedCategory == "Все" || mappedCategory == "Hammasi") {
            productDao.getAllProducts()
        } else {
            productDao.getProductsByCategory(mappedCategory)
        }
        return flow.map { entities -> entities.map { it.toDomain() } }
    }

    override fun searchProducts(query: String): Flow<List<Product>> {
        return productDao.searchProducts("%$query%").map { entities -> 
            entities.map { it.toDomain() } 
        }
    }

    override fun getProductById(id: String): Flow<Product?> {
        return productDao.getProductById(id).map { it?.toDomain() }
    }

    override fun getCartProducts(): Flow<List<Product>> {
        return productDao.getCartProducts().map { entities -> entities.map { it.toDomain() } }
    }

    override fun getSellerProducts(sellerId: String): Flow<List<Product>> {
        return productDao.getAllProducts().map { entities ->
            val all = entities.map { it.toDomain() }
            val sellerSpecific = all.filter { it.sellerId == sellerId }
            if (sellerSpecific.isNotEmpty()) {
                sellerSpecific
            } else {
                // If specific seller has no items in local DB, return first 4 products as sample seller store items
                all.take(4)
            }
        }
    }

    override fun uploadProduct(product: Product, imageUriString: String?): Flow<Resource<Product>> = flow {
        emit(Resource.Loading)
        try {
            var finalImageUrl = if (!imageUriString.isNullOrEmpty()) imageUriString else product.imageUrl

            if (!imageUriString.isNullOrEmpty() && (imageUriString.startsWith("content://") || imageUriString.startsWith("file://"))) {
                try {
                    val uri = Uri.parse(imageUriString)
                    val storageRef = firebaseStorage.reference.child("products/${product.id}.jpg")
                    val inputStream = context.contentResolver.openInputStream(uri)
                    if (inputStream != null) {
                        storageRef.putStream(inputStream).await()
                        inputStream.close()
                        finalImageUrl = storageRef.downloadUrl.await().toString()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            if (finalImageUrl.isBlank()) {
                finalImageUrl = "https://images.pexels.com/photos/106399/pexels-photo-106399.jpeg"
            }

            val finalProduct = product.copy(imageUrl = finalImageUrl)

            val productMap = mapOf(
                "id" to finalProduct.id,
                "name" to finalProduct.name,
                "price" to finalProduct.price,
                "currency" to finalProduct.currency,
                "discount" to finalProduct.discount,
                "rating" to finalProduct.rating.toDouble(),
                "installment" to (finalProduct.installment ?: ""),
                "imageUrl" to finalProduct.imageUrl,
                "category" to finalProduct.category,
                "location" to finalProduct.location,
                "description" to finalProduct.description,
                "sellerId" to finalProduct.sellerId,
                "isFavorite" to finalProduct.isFavorite,
                "isInCart" to finalProduct.isInCart,
                "isSold" to finalProduct.isSold,
                "timestamp" to finalProduct.timestamp
            )

            firestore.collection("products").document(finalProduct.id).set(productMap).await()
            productDao.insertProducts(listOf(ProductEntity.fromDomain(finalProduct)))

            emit(Resource.Success(finalProduct))
        } catch (e: Exception) {
            emit(Resource.Error(parseFirebaseError(e)))
        }
    }.flowOn(Dispatchers.IO)

    override fun getReviewsForProduct(productId: String): Flow<List<Review>> = flow {
        try {
            val querySnapshot = firestore.collection("products").document(productId)
                .collection("reviews").get().await()

            if (!querySnapshot.isEmpty) {
                val reviews = querySnapshot.documents.mapNotNull { doc ->
                    val id = doc.getString("id") ?: doc.id
                    val prodId = doc.getString("productId") ?: productId
                    val userId = doc.getString("userId") ?: ""
                    val userName = doc.getString("userName") ?: "Verified Buyer"
                    val userAvatarUrl = doc.getString("userAvatarUrl")
                    val rating = doc.getDouble("rating")?.toFloat() ?: 5.0f
                    val reviewText = doc.getString("reviewText") ?: ""
                    val timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()

                    Review(
                        id = id,
                        productId = prodId,
                        userId = userId,
                        userName = userName,
                        userAvatarUrl = userAvatarUrl,
                        rating = rating,
                        reviewText = reviewText,
                        timestamp = timestamp
                    )
                }.sortedByDescending { it.timestamp }
                emit(reviews)
            } else {
                val sampleReviews = listOf(
                    Review(
                        id = "r1",
                        productId = productId,
                        userName = "Natasya Wilodra",
                        userAvatarUrl = "https://images.pexels.com/photos/774909/pexels-photo-774909.jpeg",
                        rating = 5.0f,
                        reviewText = "Ajoyib mahsulot! Yetkazib berish juda tez amalga oshirildi va sifat a'lo darajada.",
                        timestamp = System.currentTimeMillis() - 86400000
                    ),
                    Review(
                        id = "r2",
                        productId = productId,
                        userName = "Charolet Hanlin",
                        userAvatarUrl = "https://images.pexels.com/photos/1222271/pexels-photo-1222271.jpeg",
                        rating = 4.5f,
                        reviewText = "Sotuvchi juda xushmuomala. Tavsiya qilaman!",
                        timestamp = System.currentTimeMillis() - 172800000
                    )
                )
                emit(sampleReviews)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            val sampleReviews = listOf(
                Review(
                    id = "r1",
                    productId = productId,
                    userName = "Natasya Wilodra",
                    userAvatarUrl = "https://images.pexels.com/photos/774909/pexels-photo-774909.jpeg",
                    rating = 5.0f,
                    reviewText = "Ajoyib mahsulot! Sifat a'lo darajada.",
                    timestamp = System.currentTimeMillis() - 86400000
                )
            )
            emit(sampleReviews)
        }
    }.flowOn(Dispatchers.IO)

    override fun addReview(review: Review): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading)
        try {
            val reviewMap = mapOf(
                "id" to review.id,
                "productId" to review.productId,
                "userId" to review.userId,
                "userName" to review.userName,
                "userAvatarUrl" to (review.userAvatarUrl ?: ""),
                "rating" to review.rating.toDouble(),
                "reviewText" to review.reviewText,
                "timestamp" to review.timestamp
            )

            firestore.collection("products").document(review.productId)
                .collection("reviews").document(review.id).set(reviewMap).await()

            // Calculate new average rating safely
            try {
                val querySnapshot = firestore.collection("products").document(review.productId)
                    .collection("reviews").get().await()

                var totalRating = 0.0
                var count = 0
                for (doc in querySnapshot.documents) {
                    val r = doc.getDouble("rating") ?: 5.0
                    totalRating += r
                    count++
                }
                val avgRating = if (count > 0) (totalRating / count).toFloat() else 5.0f

                firestore.collection("products").document(review.productId)
                    .set(mapOf("rating" to avgRating.toDouble()), SetOptions.merge()).await()
            } catch (_: Exception) {
                // Ignore rating update failure so review creation succeeds regardless
            }

            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(parseFirebaseError(e)))
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun syncProducts(): Unit = withContext(Dispatchers.IO) {
        try {
            val querySnapshot = firestore.collection("products").get().await()
            if (!querySnapshot.isEmpty) {
                val remoteProducts = querySnapshot.documents.mapNotNull { doc ->
                    val id = doc.getString("id") ?: doc.id
                    val name = doc.getString("name") ?: return@mapNotNull null
                    val price = doc.getDouble("price") ?: 0.0
                    val currency = doc.getString("currency") ?: "uzs"
                    val discount = doc.getLong("discount")?.toInt() ?: 0
                    val rating = doc.getDouble("rating")?.toFloat() ?: 0f
                    val reviewCount = doc.getLong("reviewCount")?.toInt() ?: 0
                    val installment = doc.getString("installment")
                    val imageUrl = doc.getString("imageUrl") ?: ""
                    @Suppress("UNCHECKED_CAST")
                    val imagesList = doc.get("images") as? List<String> ?: emptyList()
                    val category = doc.getString("category") ?: "Electronics"
                    val location = doc.getString("location") ?: "Tashkent"
                    val description = doc.getString("description") ?: ""
                    val sellerId = doc.getString("sellerId") ?: ""
                    val isFavorite = doc.getBoolean("isFavorite") ?: false
                    val isInCart = doc.getBoolean("isInCart") ?: false
                    val isSold = doc.getBoolean("isSold") ?: false
                    val timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()

                    Product(
                        id = id,
                        name = name,
                        price = price,
                        currency = currency,
                        discount = discount,
                        rating = rating,
                        reviewCount = reviewCount,
                        installment = installment,
                        imageUrl = imageUrl,
                        images = imagesList,
                        category = category,
                        location = location,
                        description = description,
                        sellerId = sellerId,
                        isFavorite = isFavorite,
                        isInCart = isInCart,
                        isSold = isSold,
                        timestamp = timestamp
                    )
                }
                productDao.insertProducts(remoteProducts.map { ProductEntity.fromDomain(it) })
            } else {
                // FIRESTORE BO'SH BO'LSA - DUMMY DATALARNI CLOUD GA YOZAMIZ
                val dummyList = getDummyData()
                
                dummyList.forEach { product ->
                    val productMap = mapOf(
                        "id" to product.id,
                        "name" to product.name,
                        "price" to product.price,
                        "currency" to product.currency,
                        "discount" to product.discount,
                        "rating" to product.rating.toDouble(),
                        "reviewCount" to product.reviewCount,
                        "installment" to (product.installment ?: ""),
                        "imageUrl" to product.imageUrl,
                        "images" to product.images,
                        "category" to product.category,
                        "location" to product.location,
                        "description" to product.description,
                        "sellerId" to product.sellerId,
                        "isFavorite" to product.isFavorite,
                        "isInCart" to product.isInCart,
                        "isSold" to product.isSold,
                        "timestamp" to product.timestamp
                    )
                    firestore.collection("products").document(product.id).set(productMap).await()
                }
                
                productDao.insertProducts(dummyList.map { ProductEntity.fromDomain(it) })
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Agar internet yoki ruxsat yo'q bo'lsa mahalliy bazaga yozamiz
            val dummyList = getDummyData()
            productDao.insertProducts(dummyList.map { ProductEntity.fromDomain(it) })
        }
    }

    override suspend fun toggleFavorite(id: String, isFavorite: Boolean): Unit = withContext(Dispatchers.IO) {
        productDao.updateFavorite(id, isFavorite)
        try {
            val favData = mapOf("productId" to id, "isFavorite" to isFavorite)
            firestore.collection("user_favorites").document(id).set(favData)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun toggleCart(id: String, isInCart: Boolean): Unit = withContext(Dispatchers.IO) {
        productDao.updateCart(id, isInCart)
    }

    private fun getDummyData(): List<Product> = listOf(
        Product("1", "iPhone 15 Pro Max 256GB", 15200000.0, category = "Electronics", imageUrl = "https://images.pexels.com/photos/18525333/pexels-photo-18525333.jpeg", images = listOf("https://images.pexels.com/photos/18525333/pexels-photo-18525333.jpeg", "https://images.pexels.com/photos/788946/pexels-photo-788946.jpeg", "https://images.pexels.com/photos/699122/pexels-photo-699122.jpeg"), location = "Tashkent", discount = 5, rating = 4.9f, reviewCount = 38, attributes = mapOf("RAM" to "8GB", "Storage" to "256GB")),
        Product("2", "MacBook Air M2 Silver", 12500000.0, category = "Electronics", imageUrl = "https://images.pexels.com/photos/812264/pexels-photo-812264.jpeg", images = listOf("https://images.pexels.com/photos/812264/pexels-photo-812264.jpeg", "https://images.pexels.com/photos/18105/pexels-photo.jpg"), location = "Samarkand", isSold = true, rating = 4.8f, reviewCount = 24, attributes = mapOf("CPU" to "M2", "RAM" to "16GB")),
        Product("3", "Chevrolet Gentra 2022", 185000000.0, category = "Cars", imageUrl = "https://images.pexels.com/photos/210019/pexels-photo-210019.jpeg", images = listOf("https://images.pexels.com/photos/210019/pexels-photo-210019.jpeg", "https://images.pexels.com/photos/170811/pexels-photo-170811.jpeg", "https://images.pexels.com/photos/112460/pexels-photo-112460.jpeg"), location = "Tashkent", installment = "15 000 000 so'mdan", rating = 4.5f, reviewCount = 19, attributes = mapOf("Fuel" to "Petrol", "Year" to "2022")),
        Product("4", "Nike Air Jordan 1 Low", 2100000.0, category = "Fashion", imageUrl = "https://images.pexels.com/photos/2529148/pexels-photo-2529148.jpeg", images = listOf("https://images.pexels.com/photos/2529148/pexels-photo-2529148.jpeg", "https://images.pexels.com/photos/1456706/pexels-photo-1456706.jpeg"), location = "Bukhara", discount = 15, rating = 4.7f, reviewCount = 42, attributes = mapOf("Size" to "42", "Color" to "White/Red")),
        Product("5", "Elite House - 3 Rooms", 1250000000.0, category = "Real Estate", imageUrl = "https://images.pexels.com/photos/106399/pexels-photo-106399.jpeg", images = listOf("https://images.pexels.com/photos/106399/pexels-photo-106399.jpeg", "https://images.pexels.com/photos/1571460/pexels-photo-1571460.jpeg", "https://images.pexels.com/photos/1643383/pexels-photo-1643383.jpeg"), location = "Tashkent City", installment = "25 000 000 so'mdan", rating = 5.0f, reviewCount = 15, attributes = mapOf("Rooms" to "3", "Area" to "120m2")),
        Product("6", "Sony PlayStation 5 Slim", 6800000.0, category = "Electronics", imageUrl = "https://images.pexels.com/photos/5948332/pexels-photo-5948332.jpeg", images = listOf("https://images.pexels.com/photos/5948332/pexels-photo-5948332.jpeg", "https://images.pexels.com/photos/4219883/pexels-photo-4219883.jpeg"), location = "Tashkent", isSold = true, rating = 4.9f, reviewCount = 51),
        Product("7", "Kitchen Mixer Silver", 450000.0, category = "Home Appliances", imageUrl = "https://images.pexels.com/photos/1181244/pexels-photo-1181244.jpeg", location = "Fergana", rating = 4.2f, reviewCount = 9),
        Product("8", "Senior Android Developer", 25000000.0, category = "Jobs", imageUrl = "https://images.pexels.com/photos/1181244/pexels-photo-1181244.jpeg", location = "Remote", rating = 4.8f, reviewCount = 12, attributes = mapOf("Exp" to "5+ years", "Type" to "Full-time")),
        Product("9", "Professional House Cleaning", 300000.0, category = "Services", imageUrl = "https://images.pexels.com/photos/4099467/pexels-photo-4099467.jpeg", location = "Tashkent", rating = 4.6f, reviewCount = 27),
        Product("10", "Dyson V15 Detect Extra", 9200000.0, category = "Home Appliances", imageUrl = "https://images.pexels.com/photos/15583173/pexels-photo-15583173.jpeg", images = listOf("https://images.pexels.com/photos/15583173/pexels-photo-15583173.jpeg", "https://images.pexels.com/photos/4107120/pexels-photo-4107120.jpeg"), location = "Samarkand", discount = 10, rating = 4.7f, reviewCount = 33),
        Product("11", "Mercedes-Benz S-Class 2023", 1500000000.0, category = "Cars", imageUrl = "https://images.pexels.com/photos/112460/pexels-photo-112460.jpeg", images = listOf("https://images.pexels.com/photos/112460/pexels-photo-112460.jpeg", "https://images.pexels.com/photos/120049/pexels-photo-120049.jpeg"), location = "Tashkent", rating = 5.0f, reviewCount = 8, attributes = mapOf("Engine" to "6.0L", "Color" to "Black")),
        Product("12", "Samsung S24 Ultra", 13800000.0, category = "Electronics", imageUrl = "https://images.pexels.com/photos/18264716/pexels-photo-18264716.jpeg", images = listOf("https://images.pexels.com/photos/18264716/pexels-photo-18264716.jpeg", "https://images.pexels.com/photos/404280/pexels-photo-404280.jpeg"), location = "Namangan", discount = 8, rating = 4.8f, reviewCount = 64),
        Product("13", "Apartment in Mirabad District", 950000000.0, category = "Real Estate", imageUrl = "https://images.pexels.com/photos/5604482/pexels-photo-5604482.jpeg", images = listOf("https://images.pexels.com/photos/5604482/pexels-photo-5604482.jpeg", "https://images.pexels.com/photos/1571460/pexels-photo-1571460.jpeg"), location = "Mirabad District", installment = "18 000 000 so'mdan", rating = 4.9f, reviewCount = 14),
        Product("14", "Logistics Manager", 12000000.0, category = "Jobs", imageUrl = "https://images.pexels.com/photos/5669332/pexels-photo-5669332.jpeg", location = "Tashkent", rating = 4.4f, reviewCount = 6),
        Product("15", "Graphic Design Course", 1500000.0, category = "Services", imageUrl = "https://images.pexels.com/photos/5176947/pexels-photo-5176947.jpeg", location = "Online", rating = 4.7f, reviewCount = 22),
        Product("16", "Tesla Model 3 Performance", 480000000.0, category = "Cars", imageUrl = "https://images.pexels.com/photos/1560958/pexels-photo-1560958.jpeg", images = listOf("https://images.pexels.com/photos/1560958/pexels-photo-1560958.jpeg", "https://images.pexels.com/photos/2526128/pexels-photo-2526128.jpeg"), location = "Tashkent", isSold = true, rating = 4.9f, reviewCount = 18)
    )
}
