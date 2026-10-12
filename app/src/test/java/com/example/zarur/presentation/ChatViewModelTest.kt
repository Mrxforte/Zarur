package com.example.zarur.presentation

import com.example.zarur.domain.model.ChatMessage
import com.example.zarur.domain.repository.ChatRepository
import com.example.zarur.domain.usecase.IsLoggedInUseCase
import com.example.zarur.presentation.message.ChatViewModel
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
class ChatViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val chatRepository: ChatRepository = mockk(relaxed = true)
    private val isLoggedInUseCase: IsLoggedInUseCase = mockk(relaxed = true)

    private lateinit var viewModel: ChatViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { chatRepository.getConversations() } returns flowOf(emptyList())
        every { chatRepository.getMessages("default_chat_id") } returns flowOf(emptyList())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testLoadMessages_empty() = runTest {
        every { chatRepository.getMessages("default_chat_id") } returns flowOf(emptyList())

        viewModel = ChatViewModel(chatRepository, isLoggedInUseCase)
        advanceUntilIdle()

        assertTrue(viewModel.messages.value.isEmpty())
    }

    @Test
    fun testLoadMessages_nonEmpty() = runTest {
        val msgs = listOf(ChatMessage(id = "1", text = "Hello"))
        every { chatRepository.getMessages("default_chat_id") } returns flowOf(msgs)

        viewModel = ChatViewModel(chatRepository, isLoggedInUseCase)
        advanceUntilIdle()

        assertEquals(1, viewModel.messages.value.size)
        assertEquals("Hello", viewModel.messages.value[0].text)
    }

    @Test
    fun testSendMessage() = runTest {
        val msgs = listOf(ChatMessage(id = "1", text = "Hi"))
        every { chatRepository.getMessages("default_chat_id") } returns flowOf(msgs)

        viewModel = ChatViewModel(chatRepository, isLoggedInUseCase)
        advanceUntilIdle()

        viewModel.sendMessage("How much is this?")
        advanceUntilIdle()

        coVerify { chatRepository.sendMessage("default_chat_id", match { it.text == "How much is this?" }) }

        viewModel.sendMessage("   ") // Blank message ignored
    }
}
