package com.example.zarur.presentation.message

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.zarur.R
import com.example.zarur.databinding.FragmentMessageBinding
import com.example.zarur.domain.usecase.IsLoggedInUseCase
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MessageFragment : Fragment(R.layout.fragment_message) {

    private var _binding: FragmentMessageBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var isLoggedInUseCase: IsLoggedInUseCase

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentMessageBinding.bind(view)

        if (!isLoggedInUseCase()) {
            findNavController().navigate(R.id.authHubFragment)
            return
        }

        binding.rvChats.layoutManager = LinearLayoutManager(context)

        val chats = listOf(
            Chat("Natasya Wilodra", "Of course, the apartment is...", "16:00", 2, R.drawable.onboarding1),
            Chat("Charolette Hanlin", "Okay, Do we have a deal?", "14:45", 0, R.drawable.onboarding2),
            Chat("Elmer Laverty", "It's nice working with you", "10:38", 0, R.drawable.onboarding3),
            Chat("Alleen Fullbright", "Will the contract be sent?", "Yesterday", 0, R.drawable.onboarding1),
            Chat("Tanner Stafford", "Wow, this is really epic", "Yesterday", 0, R.drawable.onboarding2)
        )

        binding.rvChats.adapter = ChatAdapter(chats) {
            findNavController().navigate(R.id.chatDetailFragment)
        }

        binding.shimmerChat.root.isVisible = true
        binding.rvChats.isVisible = false
        binding.shimmerChat.shimmerChatContainer.startShimmer()
        binding.rvChats.postDelayed({
            if (_binding != null) {
                binding.shimmerChat.shimmerChatContainer.stopShimmer()
                binding.shimmerChat.root.isVisible = false
                binding.rvChats.isVisible = true
            }
        }, 1000)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private data class Chat(
        val name: String,
        val lastMessage: String,
        val time: String,
        val unreadCount: Int,
        val avatar: Int
    )

    private inner class ChatAdapter(private val chats: List<Chat>, private val onClick: () -> Unit) :
        RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat, parent, false)
            return ChatViewHolder(view)
        }

        override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
            val chat = chats[position]
            holder.bind(chat)
            holder.itemView.setOnClickListener { onClick() }
        }

        override fun getItemCount(): Int = chats.size

        inner class ChatViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            private val ivAvatar = view.findViewById<ImageView>(R.id.ivAvatar)
            private val tvName = view.findViewById<TextView>(R.id.tvName)
            private val tvLastMessage = view.findViewById<TextView>(R.id.tvLastMessage)
            private val tvTime = view.findViewById<TextView>(R.id.tvTime)
            private val tvUnreadCount = view.findViewById<TextView>(R.id.tvUnreadCount)

            fun bind(chat: Chat) {
                ivAvatar.setImageResource(chat.avatar)
                tvName.text = chat.name
                tvLastMessage.text = chat.lastMessage
                tvTime.text = chat.time
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
