package com.example.zarur.presentation.profile

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.zarur.R
import com.example.zarur.domain.usecase.IsLoggedInUseCase
import com.example.zarur.presentation.common.UiState
import com.google.android.material.materialswitch.MaterialSwitch
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private val profileViewModel: ProfileViewModel by activityViewModels()
    private val languageViewModel: LanguageViewModel by viewModels()

    @Inject
    lateinit var isLoggedInUseCase: IsLoggedInUseCase

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!isLoggedInUseCase()) {
            findNavController().navigate(R.id.authHubFragment)
            return
        }

        val switchDarkMode = view.findViewById<MaterialSwitch>(R.id.switchDarkMode)
        val tvLang = view.findViewById<TextView>(R.id.tvLang)
        val tvName = view.findViewById<TextView>(R.id.tvName)
        val ivAvatar = view.findViewById<ImageView>(R.id.ivAvatar)

        // Observe Theme
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                profileViewModel.isDarkMode.collect { isDark ->
                    if (switchDarkMode.isChecked != isDark) {
                        switchDarkMode.isChecked = isDark
                    }
                }
            }
        }

        // Observe Profile Data from Firebase
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                profileViewModel.profileState.collect { uiState ->
                    if (uiState is UiState.Success) {
                        val user = uiState.data
                        tvName.text = user.fullName.ifEmpty { user.email }
                        if (!user.avatarUrl.isNullOrEmpty()) {
                            Glide.with(this@ProfileFragment)
                                .load(user.avatarUrl)
                                .placeholder(R.drawable.ic_profile)
                                .into(ivAvatar)
                        }
                    }
                }
            }
        }

        // Observe Language
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                languageViewModel.selectedLanguage.collect { language ->
                    tvLang.text = when(language) {
                        "ru" -> "Русский"
                        "uz" -> "O'zbekcha"
                        else -> "English"
                    }
                }
            }
        }

        // Handle theme toggle
        switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            profileViewModel.toggleTheme(isChecked)
            applyTheme(isChecked)
        }

        setupClickListeners(view)
    }

    private fun applyTheme(isDark: Boolean) {
        val mode = if (isDark) {
            AppCompatDelegate.MODE_NIGHT_YES
        } else {
            AppCompatDelegate.MODE_NIGHT_NO
        }
        AppCompatDelegate.setDefaultNightMode(mode)
    }

    private fun setupClickListeners(view: View) {
        view.findViewById<View>(R.id.menuMyBooking).setOnClickListener {
            findNavController().navigate(R.id.myBookingsFragment)
        }
        view.findViewById<View>(R.id.menuPayments).setOnClickListener {
            findNavController().navigate(R.id.paymentsFragment)
        }
        view.findViewById<View>(R.id.menuEditProfile).setOnClickListener {
            findNavController().navigate(R.id.editProfileFragment)
        }
        view.findViewById<View>(R.id.menuSellerMode).setOnClickListener {
            if (!isLoggedInUseCase()) {
                findNavController().navigate(R.id.authHubFragment)
            } else {
                findNavController().navigate(R.id.action_profileFragment_to_sellerDashboardFragment)
            }
        }
        view.findViewById<View>(R.id.menuNotification).setOnClickListener {
            findNavController().navigate(R.id.notificationsSettingsFragment)
        }
        view.findViewById<View>(R.id.menuSecurity).setOnClickListener {
            findNavController().navigate(R.id.securityFragment)
        }
        view.findViewById<View>(R.id.menuLanguage).setOnClickListener {
            findNavController().navigate(R.id.languageFragment)
        }
        view.findViewById<View>(R.id.menuHelpCenter).setOnClickListener {
            findNavController().navigate(R.id.helpCenterFragment)
        }
        view.findViewById<View>(R.id.menuInviteFriends).setOnClickListener {
            findNavController().navigate(R.id.inviteFriendsFragment)
        }
        view.findViewById<View>(R.id.menuLogout).setOnClickListener {
            LogoutBottomSheet().show(parentFragmentManager, "logout")
        }
    }
}
