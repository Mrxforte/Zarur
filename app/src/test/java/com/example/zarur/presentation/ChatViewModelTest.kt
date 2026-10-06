package com.example.zarur.presentation

import com.example.zarur.domain.model.ChatMessage
import com.example.zarur.domain.usecase.GetMessagesUseCase
import com.example.zarur.domain.usecase.IsLoggedInUseCase
import com.example.zarur.domain.usecase.SendMessageUseCase
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

    private val testDispatcher = UnconfinedTestDispatcher()

    private val getMessagesUseCase: GetMessagesUseCase = mockk()
    private val sendMessageUseCase: SendMessageUseCase = mockk()
    private val isLoggedInUseCase: IsLoggedInUseCase = mockk(relaxed = true)

    private lateinit var viewModel: ChatViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        coEvery { sendMessageUseCase(any(), any()) } returns Unit
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testLoadMessages_empty_populatesDefaults() = runTest {
        every { getMessagesUseCase("default_chat_id") } returns flowOf(emptyList())

        viewModel = ChatViewModel(getMessagesUseCase, sendMessageUseCase, isLoggedInUseCase)

        coVerify(atLeast = 1) { sendMessageUseCase("default_chat_id", any()) }
    }

    @Test
    fun testLoadMessages_nonEmpty() = runTest {
        val msgs = listOf(ChatMessage(id = "1", text = "Hello"))
        every { getMessagesUseCase("default_chat_id") } returns flowOf(msgs)

        viewModel = ChatViewModel(getMessagesUseCase, sendMessageUseCase, isLoggedInUseCase)

        assertEquals(1, viewModel.messages.value.size)
        assertEquals("Hello", viewModel.messages.value[0].text)
    }

    @Test
    fun testSendMessage() = runTest {
        every { getMessagesUseCase("default_chat_id") } returns flowOf(listOf(ChatMessage(id = "1", text = "Hi")))

        viewModel = ChatViewModel(getMessagesUseCase, sendMessageUseCase, isLoggedInUseCase)

        viewModel.sendMessage("How much is this?")
        coVerify { sendMessageUseCase("default_chat_id", match { it.text == "How much is this?" }) }

        viewModel.sendMessage("   ") // Blank message ignored
    }
}
