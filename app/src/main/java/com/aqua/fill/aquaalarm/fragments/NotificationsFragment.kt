package com.aqua.fill.aquaalarm.fragments

import android.app.AlarmManager
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.aqua.fill.aquaalarm.adapters.NotificationsAdapter
import com.aqua.fill.aquaalarm.databinding.FragmentNotificationsBinding
import com.aqua.fill.aquaalarm.models.NotificationData
import com.aqua.fill.aquaalarm.service.NotificationReceiver
import com.google.gson.Gson

class NotificationsFragment : Fragment() {
    private lateinit var binding: FragmentNotificationsBinding
    private lateinit var notificationsAdapter: NotificationsAdapter
    private lateinit var sharedPreferences: SharedPreferences
    private val gson = Gson()
    private lateinit var alarmManager: AlarmManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPreferences = requireContext().getSharedPreferences("userData", Context.MODE_PRIVATE)
        alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        notificationsAdapter = NotificationsAdapter()

        binding.notificationsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.notificationsRecyclerView.adapter = notificationsAdapter

        loadNotifications()
        binding.delete.setOnClickListener {
            showDeleteConfirmationDialog()
        }
    }

    private fun showDeleteConfirmationDialog() {
        val notifications = getSavedNotifications()
        if (notifications.isEmpty()) {
            Toast.makeText(
                requireContext(),
                "The list is empty, nothing to delete",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            AlertDialog.Builder(requireContext())
                .setTitle("Delete Notifications")
                .setMessage("Are you sure you want to delete all notifications?")
                .setPositiveButton("Yes") { _, _ ->
                    clearAllNotifications()
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }
    }

    private fun clearAllNotifications() {
        val notifications = getSavedNotifications()
        for (notification in notifications) {
            val requestCode = notification.time.toInt()
            val intent =
                Intent(requireActivity().applicationContext, NotificationReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                requireActivity().applicationContext,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
        }
        sharedPreferences.edit().remove("notifications").apply()
        clearNotifications()
    }

    private fun getSavedNotifications(): List<NotificationData> {
        val notificationsJson = sharedPreferences.getString("notifications", "[]")
        return gson.fromJson(notificationsJson, Array<NotificationData>::class.java).toList()
    }

    private fun loadNotifications() {
        val notificationsJson = sharedPreferences.getString("notifications", "[]")
        val notifications =
            Gson().fromJson(notificationsJson, Array<NotificationData>::class.java).toList()
        notificationsAdapter.submitList(notifications)
    }

    private fun clearNotifications() {
        notificationsAdapter.submitList(emptyList())
    }
}