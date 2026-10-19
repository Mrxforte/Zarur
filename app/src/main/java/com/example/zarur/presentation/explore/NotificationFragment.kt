package com.example.zarur.presentation.explore

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.zarur.R
import com.example.zarur.data.local.AppPreferences
import com.example.zarur.databinding.FragmentNotificationBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class NotificationFragment : Fragment(R.layout.fragment_notification) {

    private var _binding: FragmentNotificationBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var appPreferences: AppPreferences

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentNotificationBinding.bind(view)

        // Clear unread notification count
        appPreferences.unreadNotificationCount = 0

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.rvNotifications.layoutManager = LinearLayoutManager(context)

        val notifications = listOf(
            NotificationItem(
                title = getString(R.string.notification_1_title),
                desc = getString(R.string.notification_1_desc),
                time = getString(R.string.notification_time_just_now),
                isNew = true
            ),
            NotificationItem(
                title = getString(R.string.notification_2_title),
                desc = getString(R.string.notification_2_desc),
                time = getString(R.string.notification_time_today),
                isNew = false
            ),
            NotificationItem(
                title = getString(R.string.notification_3_title),
                desc = getString(R.string.notification_3_desc),
                time = getString(R.string.notification_time_yesterday),
                isNew = false
            )
        )

        if (notifications.isEmpty()) {
            binding.llEmpty.isVisible = true
            binding.rvNotifications.isVisible = false
        } else {
            binding.llEmpty.isVisible = false
            binding.rvNotifications.isVisible = true
            binding.rvNotifications.adapter = NotificationAdapter(notifications)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private data class NotificationItem(
        val title: String,
        val desc: String,
        val time: String,
        val isNew: Boolean
    )

    private inner class NotificationAdapter(private val items: List<NotificationItem>) :
        RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_notification, parent, false)
            return NotificationViewHolder(view)
        }

        override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
            holder.bind(items[position])
        }

        override fun getItemCount(): Int = items.size

        inner class NotificationViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            private val tvTitle = view.findViewById<TextView>(R.id.tvTitle)
            private val tvDesc = view.findViewById<TextView>(R.id.tvDesc)
            private val tvTime = view.findViewById<TextView>(R.id.tvTime)
            private val tvStatus = view.findViewById<TextView>(R.id.tvStatus)

            fun bind(item: NotificationItem) {
                tvTitle.text = item.title
                tvDesc.text = item.desc
                tvTime.text = item.time
                tvStatus.isVisible = item.isNew
            }
        }
    }
}
