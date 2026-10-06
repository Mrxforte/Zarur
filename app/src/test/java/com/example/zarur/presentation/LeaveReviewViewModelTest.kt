package com.example.zarur.presentation

import com.example.zarur.domain.model.Resource
import com.example.zarur.domain.usecase.AddReviewUseCase
import com.example.zarur.presentation.booking.LeaveReviewViewModel
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
class LeaveReviewViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val addReviewUseCase: AddReviewUseCase = mockk()

    private lateinit var viewModel: LeaveReviewViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = LeaveReviewViewModel(addReviewUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testSubmitReview_success() = runTest {
        every { addReviewUseCase("p1", 5f, "Awesome") } returns flowOf(
            Resource.Loading,
            Resource.Success(Unit)
        )

        viewModel.submitReview("p1", 5f, "Awesome")

        val state = viewModel.submitState.value
        assertTrue(state is UiState.Success)
    }

    @Test
    fun testSubmitReview_error() = runTest {
        every { addReviewUseCase("p1", 1f, "Bad") } returns flowOf(
            Resource.Error("Failed")
        )

        viewModel.submitReview("p1", 1f, "Bad")

        val state = viewModel.submitState.value
        assertTrue(state is UiState.Error)
        assertEquals("Failed", (state as UiState.Error).message)
    }

    @Test
    fun testResetState() {
        viewModel.resetState()
        assertNull(viewModel.submitState.value)
    }
}
