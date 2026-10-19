# Zarur - Real Estate & Property Management Android App
# Zarur - Мобильное приложение для поиска и управления недвижимостью

[🇺🇸 English](#english-version) | [🇷🇺 Русский](#русская-версия)

---

<a name="english-version"></a>
## 🇺🇸 English Version

**Zarur** is a modern, high-performance Android application designed for real estate discovery, property management, bookings, secure messaging, and user profile management. Built following modern architectural principles and robust security standards.

### 🚀 Key Features / Функционал
- **Splash & Onboarding**: Immersive splash screen and one-time multi-page onboarding walkthrough to introduce the app's value.
- **Authentication & Security**: Comprehensive auth flows including Sign In, Sign Up, Password Recovery, Change Password, and Social Logins (Google, credential management) powered by Firebase Auth & Credential Manager. Encrypted storage using Android Security Crypto.
- **Explore & Search**: Advanced real estate discovery feed, search suggestions, filters, recommendation sections, and map/placeholder views.
- **Property Details & Reviews**: Detailed product/property view with image galleries, pricing, descriptions, seller store view, and review submission & summaries.
- **Booking & E-Receipts**: End-to-end booking flow with multiple payment method selectors (Credit Cards, Humo, Uzcard, Visa, MasterCard, PayPal, crypto, etc.), booking status screens, and generated e-receipts.
- **Messaging & Calls**: Real-time chat messaging, chat detail views, voice call and video call simulation screens with call logs.
- **Seller Dashboard**: Dedicated seller store management and property creation/uploading tools.
- **Profile & Settings**: User profile editing, change PIN, security settings, saved payment cards, help center, invite friends, and dynamic language selection (English, Russian, Uzbek).

### 🛠️ Tech Stack & Libraries / Технологии и Библиотеки
- **Language**: Kotlin 100%
- **Architecture**: MVVM (Model-View-ViewModel) with Clean Architecture principles (Data, Domain, Presentation layers).
- **Dependency Injection**: Hilt (Google Dagger Hilt)
- **UI & Navigation**: 
  - Material 3 Components & Dynamic Theming
  - View Binding, ConstraintLayout, RecyclerView, ViewPager2
  - Android Jetpack Navigation Component (Safe Args)
- **Firebase Ecosystem**:
  - Firebase Authentication & Credential Manager / Google ID
  - Cloud Firestore & Firebase Realtime Database
  - Firebase Storage
  - Firebase Cloud Messaging (FCM) & Custom Messaging Service
  - Firebase Crashlytics & In-App Messaging
- **Local Database & Storage**:
  - Room Database (with KSP compiler)
  - AndroidX Security Crypto (`EncryptedSharedPreferences`)
- **Networking & Serialization**:
  - Retrofit 2 & OkHttp 3 Logging Interceptor
  - Gson
- **Async & Concurrency**:
  - Kotlin Coroutines & Flow
  - Kotlinx Coroutines Play Services
- **UI Utilities & Performance**:
  - Glide (Image loading)
  - Facebook Shimmer (Loading skeletons)
  - WorkManager (Background jobs)
  - LeakCanary (Memory leak detection in debug)
- **Testing & Quality Assurance**:
  - JUnit, MockK, Coroutines Test, AndroidX Core Testing
  - Espresso UI Testing
  - JaCoCo Code Coverage reporting

---

<a name="русская-версия"></a>
## 🇷🇺 Русская версия

**Zarur** — это современное, высокопроизводительное Android-приложение для поиска недвижимости, управления бронированиями, защищенного чата и настройки профиля пользователя. Разработано с соблюдением современных архитектурных принципов и стандартов безопасности.

### 🚀 Основные функции
- **Заставка и Онбординг**: Иммерсивный экран загрузки (Splash) и интерактивный ознакомительный флоу при первом запуске приложения.
- **Аутентификация и Безопасность**: Полный цикл авторизации (вход, регистрация, восстановление и смена пароля, социальные сети) с использованием Firebase Auth и Credential Manager. Шифрование данных через Android Security Crypto.
- **Поиск и Исследование (Explore)**: Удобная лента недвижимости, фильтры, поисковые подсказки, рекомендации и интерактивные категории.
- **Детали объекта и Отзывы**: Подробная карточка недвижимости с галереей изображений, ценами, описанием, витриной продавца, возможностью добавления и просмотра отзывов.
- **Бронирование и Электронные чеки**: Полный цикл бронирования с выбором способов оплаты (Банковские карты, Humo, Uzcard, Visa, MasterCard, PayPal, крипто-кошельки и др.), экранами статуса и генерацией электронных чеков (E-Receipt).
- **Чаты и Звонки**: Система обмена сообщениями в реальном времени, подробный чат, экраны аудио- и видеозвонков с историей вызовов.
- **Кабинет продавца (Seller Dashboard)**: Управление собственным магазином и добавление новых объектов недвижимости.
- **Профиль и Настройки**: Редактирование профиля, смена PIN-кода, настройки безопасности, управление сохраненными картами, центр помощи, приглашение друзей и смена языка интерфейса (английский, русский, узбекский).

### 🛠️ Стек технологий и библиотеки
- **Язык программирования**: Kotlin
- **Архитектура**: MVVM + Clean Architecture (слои Data, Domain, Presentation).
- **Внедрение зависимостей (DI)**: Hilt (Dagger Hilt).
- **Интерфейс и Навигация**:
  - Компоненты Material 3 и динамические темы
  - View Binding, ConstraintLayout, RecyclerView, ViewPager2
  - Jetpack Navigation Component (Safe Args)
- **Экосистема Firebase**:
  - Firebase Authentication, Credential Manager / Google ID
  - Cloud Firestore и Firebase Realtime Database
  - Firebase Storage
  - Firebase Cloud Messaging (FCM)
  - Firebase Crashlytics и In-App Messaging
- **Локальные данные и хранение**:
  - Room Database (с использованием KSP)
  - AndroidX Security Crypto (`EncryptedSharedPreferences`)
- **Сеть и сериализация**:
  - Retrofit 2 & OkHttp 3 Logging Interceptor
  - Gson
- **Асинхронность**:
  - Kotlin Coroutines & Flow
  - Coroutines Play Services
- **Утилиты интерфейса**:
  - Glide (Загрузка изображений)
  - Facebook Shimmer (Скелетоны загрузки)
  - WorkManager (Фоновые задачи)
  - LeakCanary (Поиск утечек памяти в debug-сборках)
- **Тестирование и Качество кода**:
  - JUnit, MockK, Coroutines Test, AndroidX Core Testing
  - Espresso UI Testing
  - Отчеты о покрытии кода JaCoCo

---
*Built with ❤️ for a premium real estate discovery experience.*
