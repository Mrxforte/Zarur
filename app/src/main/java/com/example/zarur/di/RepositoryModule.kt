package com.example.zarur.di

import com.example.zarur.data.repository.AuthRepositoryImpl
import com.example.zarur.data.repository.ChatRepositoryImpl
import com.example.zarur.data.repository.LanguageRepositoryImpl
import com.example.zarur.data.repository.PaymentRepositoryImpl
import com.example.zarur.data.repository.PinRepositoryImpl
import com.example.zarur.data.repository.ProductRepositoryImpl
import com.example.zarur.data.repository.ThemeRepositoryImpl
import com.example.zarur.data.repository.UserRepositoryImpl
import com.example.zarur.domain.repository.AuthRepository
import com.example.zarur.domain.repository.ChatRepository
import com.example.zarur.domain.repository.LanguageRepository
import com.example.zarur.domain.repository.PaymentRepository
import com.example.zarur.domain.repository.PinRepository
import com.example.zarur.domain.repository.ProductRepository
import com.example.zarur.domain.repository.ThemeRepository
import com.example.zarur.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        userRepositoryImpl: UserRepositoryImpl
    ): UserRepository

    @Binds
    @Singleton
    abstract fun bindThemeRepository(
        themeRepositoryImpl: ThemeRepositoryImpl
    ): ThemeRepository

    @Binds
    @Singleton
    abstract fun bindLanguageRepository(
        languageRepositoryImpl: LanguageRepositoryImpl
    ): LanguageRepository

    @Binds
    @Singleton
    abstract fun bindPinRepository(
        pinRepositoryImpl: PinRepositoryImpl
    ): PinRepository

    @Binds
    @Singleton
    abstract fun bindProductRepository(
        productRepositoryImpl: ProductRepositoryImpl
    ): ProductRepository

    @Binds
    @Singleton
    abstract fun bindChatRepository(
        chatRepositoryImpl: ChatRepositoryImpl
    ): ChatRepository

    @Binds
    @Singleton
    abstract fun bindPaymentRepository(
        paymentRepositoryImpl: PaymentRepositoryImpl
    ): PaymentRepository
}
