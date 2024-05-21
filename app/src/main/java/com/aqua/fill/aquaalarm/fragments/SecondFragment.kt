package com.aqua.fill.aquaalarm.fragments

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.aqua.fill.aquaalarm.R
import com.aqua.fill.aquaalarm.databinding.FragmentSecondBinding
import com.aqua.fill.aquaalarm.models.NotificationData
import com.aqua.fill.aquaalarm.service.NotificationReceiver
import com.aqua.fill.aquaalarm.service.channelId
import com.google.gson.Gson
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

class SecondFragment : Fragment() {
    private lateinit var binding: FragmentSecondBinding
    private lateinit var sharedPreferences: SharedPreferences
    private var dailyWaterIntake: Int = 0
    private lateinit var alarmManager: AlarmManager
    private var countDrinkWater: Int = 0
    private val gson = Gson()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSecondBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        getDay()
        showDailyWaterIntake()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }
        clicks()
        setupOnBackPressed()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotificationPermission()
        }
    }

    private fun getNotificationCount(): Int {
        val notificationsJson = sharedPreferences.getString("notifications", "[]")
        val notifications = gson.fromJson(notificationsJson, Array<NotificationData>::class.java)
        return notifications.size
    }

    private fun clicks() {
        binding.submitBtn.setOnClickListener {
            if (getNotificationCount() >= 20) {
                Toast.makeText(
                    requireContext(),
                    "You can only add up to 20 notifications.",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                val selectedTime = getTime()
                val message = "Time to drink water"
                scheduleNotification(selectedTime, message)
            }
        }
        binding.toList.setOnClickListener {
            findNavController().navigate(R.id.action_secondFragment_to_notificationsFragment)
        }
    }

    @SuppressLint("ScheduleExactAlarm")
    private fun scheduleNotification(time: Long, message: String) {
        val requestCode = time.toInt()
        val intent =
            Intent(requireActivity().applicationContext, NotificationReceiver::class.java).apply {
                putExtra("time", time)
            }

        val pendingIntent = PendingIntent.getBroadcast(
            requireActivity().applicationContext,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, pendingIntent)

        saveNotification(time, message)
        showAlert(time, message)

        lifecycleScope.launch {
            binding.textView.visibility = View.VISIBLE
            binding.textView.text = "It's time to drink water"
            delay(5000)
            binding.textView.visibility = View.INVISIBLE
        }
    }

    private fun saveNotification(time: Long, message: String) {
        val notifications = getSavedNotifications().toMutableList()
        notifications.add(NotificationData(time, message))
        sharedPreferences.edit().putString("notifications", gson.toJson(notifications)).apply()
    }

    private fun getSavedNotifications(): List<NotificationData> {
        val notificationsJson = sharedPreferences.getString("notifications", "[]")
        return gson.fromJson(notificationsJson, Array<NotificationData>::class.java).toList()
    }

    private fun showAlert(time: Long, message: String) {
        val date = Date(time)
        val timeFormat =
            android.text.format.DateFormat.getTimeFormat(requireActivity().applicationContext)

        AlertDialog.Builder(requireActivity())
            .setTitle("Notification Scheduled")
            .setMessage("Message: $message\nAt: ${timeFormat.format(date)}")
            .setPositiveButton("Ok") { _, _ -> }
            .show()
    }

    private fun getTime(): Long {
        val minute = binding.timePicker.minute
        val hour = binding.timePicker.hour
        val day = binding.datePicker.dayOfMonth
        val month = binding.datePicker.month
        val year = binding.datePicker.year

        val calendar = Calendar.getInstance()
        calendar.set(year, month, day, hour, minute)
        return calendar.timeInMillis
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Notif channel"
            val desc = "A Description of the Channel"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = desc
            }

            val notificationManager =
                requireActivity().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        } else {
            val notificationBuilder = NotificationCompat.Builder(requireContext(), channelId)
                .setContentTitle("Notification Title")
                .setContentText("Notification Text")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)

            val notificationManager =
                requireActivity().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(1, notificationBuilder.build())
        }
    }

    private fun showDailyWaterIntake() {
        binding.dailyWaterIntakeTextView.text =
            getString(R.string.daily_water_intake, dailyWaterIntake)
    }

    private fun init() {
        sharedPreferences = requireContext().getSharedPreferences("userData", Context.MODE_PRIVATE)
        alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
    }

    private fun getDay() {
        val calendar = Calendar.getInstance()
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val dayOfWeekText = when (dayOfWeek) {
            Calendar.SUNDAY -> "Sunday"
            Calendar.MONDAY -> "Monday"
            Calendar.TUESDAY -> "Tuesday"
            Calendar.WEDNESDAY -> "Wednesday"
            Calendar.THURSDAY -> "Thursday"
            Calendar.FRIDAY -> "Friday"
            Calendar.SATURDAY -> "Saturday"
            else -> "Unable to determine"
        }

        binding.tvDayOfWeekText.text = dayOfWeekText

        val weight = sharedPreferences.getFloat("weight", 0f)
        val gender = sharedPreferences.getString("gender", "Male") ?: "Male"
        val age = sharedPreferences.getInt("age", 0)
        dailyWaterIntake = calculateDailyWaterIntake(weight, gender, age)
        countDrinkWater = calculateWaterIntake(weight, gender, age)
    }

    private fun calculateDailyWaterIntake(weight: Float, gender: String, age: Int): Int {
        var dailyIntake: Int = when (gender) {
            "Male" -> (weight * 35).toInt()
            "Female" -> (weight * 31).toInt()
            else -> (weight * 35).toInt()
        }
        dailyIntake += when {
            age < 30 -> -100
            age in 30..55 -> 0
            else -> 100
        }
        return dailyIntake
    }

    private fun calculateWaterIntake(weight: Float, gender: String, age: Int): Int {
        val waterPerKg = when (gender) {
            "Male" -> 35
            "Female" -> 31
            else -> 35
        }

        val baseWaterIntake = (waterPerKg * weight).toInt()
        val ageCorrection = when {
            age < 30 -> -100
            age in 30..55 -> 0
            else -> 100
        }

        val totalWaterIntake = baseWaterIntake + ageCorrection
        val glassVolumeMl = 250
        return totalWaterIntake / glassVolumeMl
    }

    private fun setupOnBackPressed() {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                requireActivity().finish()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun requestNotificationPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                REQUEST_NOTIFICATION_PERMISSION
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_NOTIFICATION_PERMISSION) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                Toast.makeText(
                    requireContext(),
                    "Notification permission granted",
                    Toast.LENGTH_SHORT
                ).show()

                if (getNotificationCount() >= 20) {
                    Toast.makeText(
                        requireContext(),
                        "You can only add up to 20 notifications.",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    val selectedTime = getTime()
                    val message = "Time to drink water"
                    scheduleNotification(selectedTime, message)
                }

            } else {
                Toast.makeText(
                    requireContext(),
                    "Notification permission denied",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    companion object {
        private const val REQUEST_NOTIFICATION_PERMISSION = 1
    }
}