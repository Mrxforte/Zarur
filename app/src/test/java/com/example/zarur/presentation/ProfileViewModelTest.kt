package com.example.zarur.presentation

import com.example.zarur.domain.model.Resource
import com.example.zarur.domain.model.User
import com.example.zarur.domain.usecase.*
import com.example.zarur.presentation.common.UiState
import com.example.zarur.presentation.profile.ProfileViewModel
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
class ProfileViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private val getThemeUseCase: GetThemeUseCase = mockk()
    private val setThemeUseCase: SetThemeUseCase = mockk()
    private val getUserProfileUseCase: GetUserProfileUseCase = mockk()
    private val updateUserProfileUseCase: UpdateUserProfileUseCase = mockk()
    private val signOutUseCase: SignOutUseCase = mockk()

    private lateinit var viewModel: ProfileViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        every { getThemeUseCase() } returns flowOf(true)
        every { getUserProfileUseCase() } returns flowOf(Resource.Success(User(id = "u1", fullName = "Andrew")))
        coEvery { setThemeUseCase(any()) } returns Unit
        every { signOutUseCase() } returns Unit

        viewModel = ProfileViewModel(
            getThemeUseCase,
            setThemeUseCase,
            getUserProfileUseCase,
            updateUserProfileUseCase,
            signOutUseCase
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testIsDarkMode_andLoadProfile() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.isDarkMode.collect() }
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.profileState.collect() }

        assertTrue(viewModel.isDarkMode.value)

        val profileState = viewModel.profileState.value
        assertTrue(profileState is UiState.Success)
        assertEquals("Andrew", (profileState as UiState.Success).data.fullName)
    }

    @Test
    fun testToggleTheme() = runTest {
        viewModel.toggleTheme(false)
        coVerify { setThemeUseCase(false) }
    }

    @Test
    fun testUpdateProfile_success() = runTest {
        val events = mutableListOf<UiState<Unit>>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.updateEvent.collect { events.add(it) } }

        viewModel.setSelectedAvatar("content://avatar.jpg")
        every {
            updateUserProfileUseCase(
                fullName = "New Name",
                nickname = "Nick",
                email = "email@test.com",
                phone = "123",
                gender = "M",
                dob = "2000-01-01",
                imageUri = "content://avatar.jpg"
            )
        } returns flowOf(Resource.Loading, Resource.Success(Unit))

        viewModel.updateProfile("New Name", "Nick", "email@test.com", "123", "M", "2000-01-01")

        assertTrue(events.isNotEmpty())
        assertTrue(events.last() is UiState.Success)
    }

    @Test
    fun testSignOut() {
        viewModel.signOut()
        verify { signOutUseCase() }
    }
}
