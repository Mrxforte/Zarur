package com.example.zarur.presentation.message

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.zarur.R
import com.example.zarur.domain.model.ChatMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ChatDetailFragment : Fragment(R.layout.fragment_chat_detail) {

    private val viewModel: ChatViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<ImageView>(R.id.ivBack).setOnClickListener {
            findNavController().popBackStack()
        }

        view.findViewById<ImageView>(R.id.ivVoiceCall).setOnClickListener {
            if (!viewModel.isLoggedIn()) {
                findNavController().navigate(R.id.authHubFragment)
            } else {
                findNavController().navigate(R.id.action_chatDetailFragment_to_voiceCallFragment)
            }
        }

        view.findViewById<ImageView>(R.id.ivVideoCall).setOnClickListener {
            if (!viewModel.isLoggedIn()) {
                findNavController().navigate(R.id.authHubFragment)
            } else {
                findNavController().navigate(R.id.action_chatDetailFragment_to_videoCallFragment)
            }
        }

        val rvMessages = view.findViewById<RecyclerView>(R.id.rvMessages)
        val linearLayoutManager = LinearLayoutManager(context).apply {
            stackFromEnd = true
        }
        rvMessages.layoutManager = linearLayoutManager

        val adapter = MessageAdapter(emptyList())
        rvMessages.adapter = adapter

        val etMessage = view.findViewById<EditText>(R.id.etMessage)
        view.findViewById<ImageView>(R.id.ivSend).setOnClickListener {
            if (!viewModel.isLoggedIn()) {
                findNavController().navigate(R.id.authHubFragment)
            } else {
                val text = etMessage.text.toString().trim()
                if (text.isNotEmpty()) {
                    viewModel.sendMessage(text)
                    etMessage.setText("")
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.messages.collect { messages ->
                    adapter.updateMessages(messages)
                    if (messages.isNotEmpty()) {
                        rvMessages.scrollToPosition(messages.size - 1)
                    }
                }
            }
        }
    }

    private class MessageAdapter(private var messages: List<ChatMessage>) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        fun updateMessages(newMessages: List<ChatMessage>) {
            messages = newMessages
            notifyDataSetChanged()
        }

        override fun getItemViewType(position: Int): Int {
            return when {
                messages[position].isProperty -> 2
                messages[position].isSent -> 0
                else -> 1
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val layout = when (viewType) {
                0 -> R.layout.item_message_sent
                1 -> R.layout.item_message_received
                else -> R.layout.item_message_property
            }
            val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)
            return object : RecyclerView.ViewHolder(view) {}
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val message = messages[position]
            if (!message.isProperty) {
                holder.itemView.findViewById<TextView>(R.id.tvMessage).text = message.text
                holder.itemView.findViewById<TextView>(R.id.tvTime).text = message.time
            }
        }

        override fun getItemCount(): Int = messages.size
    }
}
