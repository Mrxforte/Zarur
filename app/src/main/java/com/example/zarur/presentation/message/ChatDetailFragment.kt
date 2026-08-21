package com.example.zarur.presentation.message

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.zarur.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChatDetailFragment : Fragment(R.layout.fragment_chat_detail) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<ImageView>(R.id.ivBack).setOnClickListener {
            findNavController().popBackStack()
        }

        view.findViewById<ImageView>(R.id.ivVoiceCall).setOnClickListener {
            findNavController().navigate(R.id.action_chatDetailFragment_to_voiceCallFragment)
        }

        view.findViewById<ImageView>(R.id.ivVideoCall).setOnClickListener {
            findNavController().navigate(R.id.action_chatDetailFragment_to_videoCallFragment)
        }

        val rvMessages = view.findViewById<RecyclerView>(R.id.rvMessages)
        rvMessages.layoutManager = LinearLayoutManager(context).apply {
            stackFromEnd = true
        }
        
        val messages = listOf(
            Message("PROPERTY", "", false, isProperty = true),
            Message("I want to book your apartment for 5 days. Can it?", "16:00", true),
            Message("Hello, good afternoon too Andrew..", "16:01", false),
            Message("Of course, the apartment is always open anytime", "16:01", false),
            Message("Great! I will wait for your booking and arrival!", "16:03", true)
        )
        
        rvMessages.adapter = MessageAdapter(messages)
    }

    private data class Message(
        val text: String,
        val time: String,
        val isSent: Boolean,
        val isProperty: Boolean = false
    )

    private inner class MessageAdapter(private val messages: List<Message>) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {

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
