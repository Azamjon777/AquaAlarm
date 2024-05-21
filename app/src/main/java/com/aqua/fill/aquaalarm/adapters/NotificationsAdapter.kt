package com.aqua.fill.aquaalarm.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.aqua.fill.aquaalarm.databinding.ItemNotificationBinding
import com.aqua.fill.aquaalarm.models.NotificationData
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NotificationsAdapter :
    ListAdapter<NotificationData, NotificationsAdapter.NotificationViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val binding =
            ItemNotificationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NotificationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val notification = getItem(position)
        holder.bind(notification)
    }

    class NotificationViewHolder(private val binding: ItemNotificationBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(notification: NotificationData) {
            val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            val timeString = timeFormat.format(Date(notification.time))
            binding.notificationTime.text = timeString
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<NotificationData>() {
        override fun areItemsTheSame(
            oldItem: NotificationData,
            newItem: NotificationData
        ): Boolean {
            return oldItem.time == newItem.time
        }

        override fun areContentsTheSame(
            oldItem: NotificationData,
            newItem: NotificationData
        ): Boolean {
            return oldItem == newItem
        }
    }
}