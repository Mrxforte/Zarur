# Implementation Plan - Separate Favorites and Cart Tabs on Favorites Screen (Clean Architecture)

Add cart functionality and integrate cart products into the Favorites screen with separate tabs/views for **Favorites** and **Cart** using Clean Architecture principles.

## User Review Required

> [!IMPORTANT]
> - **Cart Data & Use Cases**: Add `isInCart` flag to `Product` domain model and `ProductEntity` Room table. Add `GetCartProductsUseCase` and `ToggleCartUseCase`.
> - **UI / Screen Separation**: Add a Segmented Button / Tab switcher ("Favorites" vs "Cart") on the Favorites screen (`FavoritesFragment`) to view favorites and added cart items separately.
> - **Product Detail**: Clicking "Add to Cart" will mark the product as in the cart and show success.

## Proposed Changes

### [Domain Layer]
- **[MODIFY] [Product.kt](file:///C:/Users/Azamat/AndroidStudioProjects/Zarur/app/src/main/java/com/example/zarur/domain/model/Product.kt)**: Add `isInCart: Boolean = false`.
- **[NEW] [GetCartProductsUseCase.kt](file:///C:/Users/Azamat/AndroidStudioProjects/Zarur/app/src/main/java/com/example/zarur/domain/usecase/GetCartProductsUseCase.kt)**: Fetch cart products stream.
- **[NEW] [ToggleCartUseCase.kt](file:///C:/Users/Azamat/AndroidStudioProjects/Zarur/app/src/main/java/com/example/zarur/domain/usecase/ToggleCartUseCase.kt)**: Add/remove product from cart.

### [Data Layer]
- **[MODIFY] [ProductEntity.kt](file:///C:/Users/Azamat/AndroidStudioProjects/Zarur/app/src/main/java/com/example/zarur/data/local/ProductEntity.kt)**: Add `isInCart` column mapping.
- **[MODIFY] [ProductDao.kt](file:///C:/Users/Azamat/AndroidStudioProjects/Zarur/app/src/main/java/com/example/zarur/data/local/ProductDao.kt)**: Add queries for cart products and `updateCart()`.
- **[MODIFY] [ProductDatabase.kt](file:///C:/Users/Azamat/AndroidStudioProjects/Zarur/app/src/main/java/com/example/zarur/data/local/ProductDatabase.kt)**: Enable `fallbackToDestructiveMigration()` for schema update.
- **[MODIFY] [ProductRepositoryImpl.kt](file:///C:/Users/Azamat/AndroidStudioProjects/Zarur/app/src/main/java/com/example/zarur/data/repository/ProductRepositoryImpl.kt)**: Implement `getCartProducts()` and `toggleCart()`.

### [Presentation Layer]
- **[MODIFY] [fragment_favorites.xml](file:///C:/Users/Azamat/AndroidStudioProjects/Zarur/app/src/main/res/layout/fragment_favorites.xml)**: Add a tab layout or segment buttons ("Favorites" and "Cart").
- **[MODIFY] [FavoritesViewModel.kt](file:///C:/Users/Azamat/AndroidStudioProjects/Zarur/app/src/main/java/com/example/zarur/presentation/favorites/FavoritesViewModel.kt)**: Expose both favorite products and cart products streams, and current tab state.
- **[MODIFY] [FavoritesFragment.kt](file:///C:/Users/Azamat/AndroidStudioProjects/Zarur/app/src/main/java/com/example/zarur/presentation/favorites/FavoritesFragment.kt)**: Handle tab switching between Favorites and Cart.
- **[MODIFY] [ProductDetailViewModel.kt](file:///C:/Users/Azamat/AndroidStudioProjects/Zarur/app/src/main/java/com/example/zarur/presentation/details/ProductDetailViewModel.kt)**: Update `addToCart()` to persist `toggleCartUseCase(product.id, true)`.

## Verification Plan

### Automated Tests
- Build project (`app:assembleDebug`) to verify compilation and Room database schema mapping.

### Manual Verification
- Add product to cart from Product Detail -> check Cart tab on Favorites screen.
- Verify Favorites and Cart items are displayed separately with tabs.
