package com.example.zarur.presentation

import com.example.zarur.domain.model.Resource
import com.example.zarur.domain.model.User
import com.example.zarur.domain.usecase.SignInUseCase
import com.example.zarur.domain.usecase.SignInWithGoogleUseCase
import com.example.zarur.domain.usecase.SignUpUseCase
import com.example.zarur.presentation.auth.AuthViewModel
import com.example.zarur.presentation.common.UiState
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val signInUseCase: SignInUseCase = mockk()
    private val signInWithGoogleUseCase: SignInWithGoogleUseCase = mockk()
    private val signUpUseCase: SignUpUseCase = mockk()

    private lateinit var viewModel: AuthViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = AuthViewModel(signInUseCase, signInWithGoogleUseCase, signUpUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testSignIn_success() = runTest {
        val user = User(id = "u1", email = "a@b.com")
        every { signInUseCase("a@b.com", "123456") } returns flowOf(
            Resource.Loading,
            Resource.Success(user)
        )

        viewModel.signIn("a@b.com", "123456")

        val state = viewModel.signInState.value
        assertTrue(state is UiState.Success)
        assertEquals("u1", (state as UiState.Success).data.id)
    }

    @Test
    fun testSignIn_error() = runTest {
        every { signInUseCase("a@b.com", "wrong") } returns flowOf(
            Resource.Error("Invalid credentials")
        )

        viewModel.signIn("a@b.com", "wrong")

        val state = viewModel.signInState.value
        assertTrue(state is UiState.Error)
        assertEquals("Invalid credentials", (state as UiState.Error).message)
    }

    @Test
    fun testSignInWithGoogle_success() = runTest {
        val user = User(id = "google_u1", email = "google@b.com")
        every { signInWithGoogleUseCase("google_id_token") } returns flowOf(
            Resource.Loading,
            Resource.Success(user)
        )

        viewModel.signInWithGoogle("google_id_token")

        val state = viewModel.signInState.value
        assertTrue(state is UiState.Success)
        assertEquals("google_u1", (state as UiState.Success).data.id)
    }

    @Test
    fun testSignInWithGoogle_error() = runTest {
        every { signInWithGoogleUseCase("invalid_token") } returns flowOf(
            Resource.Error("Google Sign in failed")
        )

        viewModel.signInWithGoogle("invalid_token")

        val state = viewModel.signInState.value
        assertTrue(state is UiState.Error)
        assertEquals("Google Sign in failed", (state as UiState.Error).message)
    }

    @Test
    fun testSignUp_success() = runTest {
        val user = User(id = "u2", fullName = "New User")
        every { signUpUseCase("a@b.com", "123456", "New User") } returns flowOf(
            Resource.Success(user)
        )

        viewModel.signUp("a@b.com", "123456", "New User")

        val state = viewModel.signUpState.value
        assertTrue(state is UiState.Success)
        assertEquals("New User", (state as UiState.Success).data.fullName)
    }

    @Test
    fun testSignUp_error() = runTest {
        every { signUpUseCase("a@b.com", "123456", "New User") } returns flowOf(
            Resource.Error("Email already registered")
        )

        viewModel.signUp("a@b.com", "123456", "New User")

        val state = viewModel.signUpState.value
        assertTrue(state is UiState.Error)
        assertEquals("Email already registered", (state as UiState.Error).message)
    }

    @Test
    fun testResetStates() = runTest {
        val user = User(id = "u1")
        every { signInUseCase(any(), any()) } returns flowOf(Resource.Success(user))
        every { signUpUseCase(any(), any(), any()) } returns flowOf(Resource.Success(user))

        viewModel.signIn("a@b.com", "123")
        viewModel.signUp("a@b.com", "123", "Name")

        viewModel.resetSignInState()
        viewModel.resetSignUpState()

        assertNull(viewModel.signInState.value)
        assertNull(viewModel.signUpState.value)
    }
}
