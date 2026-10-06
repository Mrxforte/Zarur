package com.example.zarur.presentation.profile

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.zarur.R
import com.example.zarur.data.local.AppPreferences
import com.example.zarur.databinding.FragmentNotificationsSettingsBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class NotificationsSettingsFragment : Fragment(R.layout.fragment_notifications_settings) {

    private var _binding: FragmentNotificationsSettingsBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var appPreferences: AppPreferences

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentNotificationsSettingsBinding.bind(view)

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        // Load saved preferences
        binding.switchGeneral.isChecked = appPreferences.notifGeneral
        binding.switchSound.isChecked = appPreferences.notifSound
        binding.switchVibrate.isChecked = appPreferences.notifVibrate
        binding.switchSpecialOffers.isChecked = appPreferences.notifSpecialOffers
        binding.switchPromoDiscount.isChecked = appPreferences.notifPromoDiscount
        binding.switchPayments.isChecked = appPreferences.notifPayments

        // Bind listeners to save changes immediately
        binding.switchGeneral.setOnCheckedChangeListener { _, isChecked ->
            appPreferences.notifGeneral = isChecked
        }

        binding.switchSound.setOnCheckedChangeListener { _, isChecked ->
            appPreferences.notifSound = isChecked
        }

        binding.switchVibrate.setOnCheckedChangeListener { _, isChecked ->
            appPreferences.notifVibrate = isChecked
        }

        binding.switchSpecialOffers.setOnCheckedChangeListener { _, isChecked ->
            appPreferences.notifSpecialOffers = isChecked
        }

        binding.switchPromoDiscount.setOnCheckedChangeListener { _, isChecked ->
            appPreferences.notifPromoDiscount = isChecked
        }

        binding.switchPayments.setOnCheckedChangeListener { _, isChecked ->
            appPreferences.notifPayments = isChecked
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
