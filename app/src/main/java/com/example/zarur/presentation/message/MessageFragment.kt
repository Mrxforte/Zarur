package com.example.zarur.presentation.message

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.zarur.R
import com.example.zarur.databinding.FragmentMessageBinding
import com.example.zarur.domain.model.ChatConversation
import com.example.zarur.domain.usecase.IsLoggedInUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MessageFragment : Fragment(R.layout.fragment_message) {

    private var _binding: FragmentMessageBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ChatViewModel by activityViewModels()

    @Inject
    lateinit var isLoggedInUseCase: IsLoggedInUseCase

    private lateinit var chatAdapter: ChatAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentMessageBinding.bind(view)

        if (!isLoggedInUseCase()) {
            findNavController().navigate(R.id.authHubFragment)
            return
        }

        setupRecyclerView()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        binding.rvChats.layoutManager = LinearLayoutManager(context)
        chatAdapter = ChatAdapter(emptyList()) { conv ->
            viewModel.loadMessages(conv.chatId)
            findNavController().navigate(R.id.action_messageFragment_to_chatDetailFragment)
        }
        binding.rvChats.adapter = chatAdapter
    }

    private fun observeViewModel() {
        binding.shimmerChat.root.isVisible = true
        binding.rvChats.isVisible = false
        binding.shimmerChat.shimmerChatContainer.startShimmer()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.conversations.collect { conversations ->
                    chatAdapter.updateChats(conversations)

                    binding.shimmerChat.shimmerChatContainer.stopShimmer()
                    binding.shimmerChat.root.isVisible = false
                    binding.rvChats.isVisible = true
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private inner class ChatAdapter(
        private var chats: List<ChatConversation>,
        private val onClick: (ChatConversation) -> Unit
    ) : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

        fun updateChats(newChats: List<ChatConversation>) {
            chats = newChats
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat, parent, false)
            return ChatViewHolder(view)
        }

        override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
            val chat = chats[position]
            holder.bind(chat)
            holder.itemView.setOnClickListener { onClick(chat) }
        }

        override fun getItemCount(): Int = chats.size

        inner class ChatViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            private val ivAvatar = view.findViewById<ImageView>(R.id.ivAvatar)
            private val tvName = view.findViewById<TextView>(R.id.tvName)
            private val tvLastMessage = view.findViewById<TextView>(R.id.tvLastMessage)
            private val tvTime = view.findViewById<TextView>(R.id.tvTime)
            private val tvUnreadCount = view.findViewById<TextView>(R.id.tvUnreadCount)

            fun bind(chat: ChatConversation) {
                ivAvatar.setImageResource(R.drawable.onboarding1)
                tvName.text = chat.sellerName
                tvLastMessage.text = chat.lastMessage
                tvTime.text = chat.lastMessageTime
                if (chat.unreadCount > 0) {
                    tvUnreadCount.visibility = View.VISIBLE
                    tvUnreadCount.text = chat.unreadCount.toString()
                } else {
                    tvUnreadCount.visibility = View.GONE
                }
            }
        }
    }
}
