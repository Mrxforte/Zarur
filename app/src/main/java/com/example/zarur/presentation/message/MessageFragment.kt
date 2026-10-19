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
import com.example.zarur.domain.model.CallLog
import com.example.zarur.domain.model.CallStatus
import com.example.zarur.domain.model.CallType
import com.example.zarur.domain.model.ChatConversation
import com.example.zarur.domain.usecase.IsLoggedInUseCase
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.example.zarur.util.showAuthRequiredDialog

@AndroidEntryPoint
class MessageFragment : Fragment(R.layout.fragment_message) {

    private var _binding: FragmentMessageBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ChatViewModel by activityViewModels()

    @Inject
    lateinit var isLoggedInUseCase: IsLoggedInUseCase

    private lateinit var chatAdapter: ChatAdapter
    private lateinit var callAdapter: CallAdapter

    private var currentTabPosition = 0
    private var currentChats: List<ChatConversation> = emptyList()
    private var currentCalls: List<CallLog> = emptyList()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentMessageBinding.bind(view)

        if (!isLoggedInUseCase()) {
            showAuthRequiredDialog()
        }

        setupRecyclerViews()
        setupTabLayout()
        observeViewModel()
    }

    private fun setupRecyclerViews() {
        binding.rvChats.layoutManager = LinearLayoutManager(context)
        chatAdapter = ChatAdapter(emptyList()) { conv ->
            viewModel.loadMessages(conv.chatId)
            findNavController().navigate(R.id.action_messageFragment_to_chatDetailFragment)
        }
        binding.rvChats.adapter = chatAdapter

        binding.rvCalls.layoutManager = LinearLayoutManager(context)
        callAdapter = CallAdapter(emptyList()) { callLog ->
            if (callLog.callType == CallType.VIDEO) {
                findNavController().navigate(R.id.action_messageFragment_to_videoCallFragment)
            } else {
                findNavController().navigate(R.id.action_messageFragment_to_voiceCallFragment)
            }
        }
        binding.rvCalls.adapter = callAdapter
    }

    private fun setupTabLayout() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                currentTabPosition = tab?.position ?: 0
                updateTabVisibility()
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun updateTabVisibility() {
        binding.shimmerChat.shimmerChatContainer.stopShimmer()
        binding.shimmerChat.root.isVisible = false

        if (currentTabPosition == 0) {
            // Chats tab
            binding.rvCalls.isVisible = false
            if (currentChats.isEmpty()) {
                binding.rvChats.isVisible = false
                binding.tvEmptyState.isVisible = true
                binding.tvEmptyState.text = getString(R.string.chat_list_empty)
            } else {
                binding.rvChats.isVisible = true
                binding.tvEmptyState.isVisible = false
            }
        } else {
            // Calls tab - ONLY show call logs, NO chat messages
            binding.rvChats.isVisible = false
            if (currentCalls.isEmpty()) {
                binding.rvCalls.isVisible = false
                binding.tvEmptyState.isVisible = true
                binding.tvEmptyState.text = getString(R.string.call_log_empty)
            } else {
                binding.rvCalls.isVisible = true
                binding.tvEmptyState.isVisible = false
            }
        }
    }

    private fun observeViewModel() {
        binding.shimmerChat.root.isVisible = true
        binding.rvChats.isVisible = false
        binding.rvCalls.isVisible = false
        binding.tvEmptyState.isVisible = false
        binding.shimmerChat.shimmerChatContainer.startShimmer()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.conversations.collect { conversations ->
                        currentChats = conversations
                        chatAdapter.updateChats(conversations)
                        updateTabVisibility()
                    }
                }
                launch {
                    viewModel.callLogs.collect { callLogs ->
                        currentCalls = callLogs
                        callAdapter.updateCalls(callLogs)
                        updateTabVisibility()
                    }
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

    private inner class CallAdapter(
        private var callLogs: List<CallLog>,
        private val onCallClick: (CallLog) -> Unit
    ) : RecyclerView.Adapter<CallAdapter.CallViewHolder>() {

        fun updateCalls(newCalls: List<CallLog>) {
            callLogs = newCalls
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CallViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_call_log, parent, false)
            return CallViewHolder(view)
        }

        override fun onBindViewHolder(holder: CallViewHolder, position: Int) {
            val callLog = callLogs[position]
            holder.bind(callLog)
            holder.itemView.setOnClickListener { onCallClick(callLog) }
        }

        override fun getItemCount(): Int = callLogs.size

        inner class CallViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            private val ivAvatar = view.findViewById<ImageView>(R.id.ivAvatar)
            private val tvName = view.findViewById<TextView>(R.id.tvName)
            private val ivCallStatusIcon = view.findViewById<ImageView>(R.id.ivCallStatusIcon)
            private val tvCallDetails = view.findViewById<TextView>(R.id.tvCallDetails)
            private val tvTime = view.findViewById<TextView>(R.id.tvTime)
            private val btnCall = view.findViewById<ImageView>(R.id.btnCall)

            fun bind(callLog: CallLog) {
                ivAvatar.setImageResource(R.drawable.onboarding1)
                tvName.text = callLog.callerName
                tvTime.text = callLog.time

                val statusStr = when (callLog.callStatus) {
                    CallStatus.INCOMING -> getString(R.string.call_status_incoming)
                    CallStatus.OUTGOING -> getString(R.string.call_status_outgoing)
                    CallStatus.MISSED -> getString(R.string.call_status_missed)
                    CallStatus.DECLINED -> getString(R.string.call_status_declined)
                }

                val typeStr = when (callLog.callType) {
                    CallType.VOICE -> getString(R.string.call_type_voice)
                    CallType.VIDEO -> getString(R.string.call_type_video)
                }

                when (callLog.callStatus) {
                    CallStatus.INCOMING -> ivCallStatusIcon.setImageResource(R.drawable.ic_call_incoming)
                    CallStatus.OUTGOING -> ivCallStatusIcon.setImageResource(R.drawable.ic_call_outgoing)
                    CallStatus.MISSED -> ivCallStatusIcon.setImageResource(R.drawable.ic_call_missed)
                    CallStatus.DECLINED -> ivCallStatusIcon.setImageResource(R.drawable.ic_call_declined)
                }

                val detailsText = if (!callLog.duration.isNullOrEmpty()) {
                    "$statusStr • ${callLog.duration}"
                } else {
                    "$statusStr • $typeStr"
                }
                tvCallDetails.text = detailsText

                if (callLog.callType == CallType.VIDEO) {
                    btnCall.setImageResource(R.drawable.ic_video)
                } else {
                    btnCall.setImageResource(R.drawable.ic_phone)
                }

                btnCall.setOnClickListener {
                    onCallClick(callLog)
                }
            }
        }
    }
}
