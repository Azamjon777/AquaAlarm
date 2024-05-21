package com.aqua.fill.aquaalarm.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.text.InputFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.aqua.fill.aquaalarm.R
import com.aqua.fill.aquaalarm.databinding.FragmentSplashBinding

class SplashFragment : Fragment() {
    private lateinit var binding: FragmentSplashBinding
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSplashBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedPreferences = requireContext().getSharedPreferences("userData", Context.MODE_PRIVATE)

        binding.weightTextInputEditText.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(3))

        if (sharedPreferences.contains("age") && sharedPreferences.contains("weight") && sharedPreferences.contains(
                "gender"
            )
        ) {
            navigateToNextScreen()
            return
        }

        binding.nextButton.setOnClickListener {
            val age = binding.ageTextInputEditText.text.toString()
            val weight = binding.weightTextInputEditText.text.toString()

            val checkedRadioButtonId = binding.genderRadioGroup.checkedRadioButtonId
            val gender = when (checkedRadioButtonId) {
                R.id.maleRadioButton -> "Male"
                R.id.femaleRadioButton -> "Female"
                else -> ""
            }

            if (age.isNotEmpty() && weight.isNotEmpty() && gender.isNotEmpty()) {
                val ageInt = age.toInt()
                val weightFloat = weight.toFloat()

                if (ageInt in 1..120 && weightFloat in 30.0..150.0) {
                    with(sharedPreferences.edit()) {
                        putInt("age", ageInt)
                        putString("gender", gender)
                        putFloat("weight", weightFloat)
                        apply()
                    }

                    navigateToNextScreen()
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Please enter valid age (1-120) and weight (30-150 kg)",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(
                    requireContext(),
                    "Please enter all data",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun navigateToNextScreen() {
        findNavController().navigate(R.id.action_splashFragment_to_firstFragment)
    }
}