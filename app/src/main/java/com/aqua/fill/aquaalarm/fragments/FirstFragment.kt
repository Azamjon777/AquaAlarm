package com.aqua.fill.aquaalarm.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.aqua.fill.aquaalarm.R
import com.aqua.fill.aquaalarm.databinding.FragmentFirstBinding
import java.util.Calendar

class FirstFragment : Fragment() {
    private lateinit var binding: FragmentFirstBinding
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedPreferences = requireContext().getSharedPreferences("userData", Context.MODE_PRIVATE)

        if (hasSavedData()) {
            navigateToNextFragment()
        } else {
            clicks()
            checkFillEditText()
            setupOnBackPressed()
        }
    }

    private fun hasSavedData(): Boolean {
        return sharedPreferences.contains(WAKE_UP) && sharedPreferences.contains(SLEEP_TIME)
    }

    private fun navigateToNextFragment() {
        findNavController().navigate(R.id.action_firstFragment_to_secondFragment)
    }

    private fun clicks() {
        binding.btnSave.setOnClickListener {
            val wakeUpHour = binding.editTextWakeUpHour.text.toString()
            val wakeUpMinute = binding.editTextWakeUpMinute.text.toString()
            val sleepHour = binding.editTextSleepHour.text.toString()
            val sleepMinute = binding.editTextSleepMinute.text.toString()

            if (validateInputs(wakeUpHour, wakeUpMinute, sleepHour, sleepMinute)) {
                val wakeUpTime = parseTime(wakeUpHour, wakeUpMinute)
                val sleepTime = parseTime(sleepHour, sleepMinute)

                saveData(wakeUpTime, sleepTime)
                Toast.makeText(requireContext(), "Data saved successfully", Toast.LENGTH_SHORT)
                    .show()
            } else {
                Toast.makeText(requireContext(), "Please enter valid times", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun checkFillEditText() {
        binding.editTextWakeUpHour.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (s?.length == 2) {
                    binding.editTextWakeUpMinute.requestFocus()
                }
            }
        })

        binding.editTextWakeUpMinute.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (s?.length == 2) {
                    binding.editTextSleepHour.requestFocus()
                }
            }
        })
        binding.editTextSleepHour.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (s?.length == 2) {
                    binding.editTextSleepMinute.requestFocus()
                }
            }
        })
    }

    private fun validateInputs(
        wakeUpHour: String,
        wakeUpMinute: String,
        sleepHour: String,
        sleepMinute: String
    ): Boolean {
        return isTimeStringValid(wakeUpHour, wakeUpMinute) && isTimeStringValid(
            sleepHour,
            sleepMinute
        )
    }

    private fun isTimeStringValid(hour: String, minute: String): Boolean {
        val regex = Regex("([01]\\d|2[0-3]):([0-5]\\d)")
        return regex.matches("$hour:$minute")
    }

    private fun parseTime(hour: String, minute: String): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, hour.toInt())
        calendar.set(Calendar.MINUTE, minute.toInt())
        calendar.set(Calendar.SECOND, 0)
        return calendar.timeInMillis
    }

    private fun saveData(wakeUpTime: Long, sleepTime: Long) {
        sharedPreferences.edit()
            .putLong("wakeUpTime", wakeUpTime)
            .putLong("sleepTime", sleepTime)
            .apply()
        findNavController().navigate(R.id.action_firstFragment_to_secondFragment)
    }

    private fun setupOnBackPressed() {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                requireActivity().finish()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }

    companion object {
        const val WAKE_UP = "wakeUpTime"
        const val SLEEP_TIME = "sleepTime"
    }
}