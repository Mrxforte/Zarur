package com.example.zarur.presentation.profile

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
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
import com.example.zarur.util.showSuccessToast
import com.google.android.material.materialswitch.MaterialSwitch
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.launch
import java.util.Locale

import com.example.zarur.util.ensureAuth
import com.example.zarur.util.showAuthRequiredDialog

@AndroidEntryPoint
class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private val profileViewModel: ProfileViewModel by activityViewModels()
    private val languageViewModel: LanguageViewModel by viewModels()

    @Inject
    lateinit var isLoggedInUseCase: IsLoggedInUseCase

    private val imagePicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val ivAvatar = view?.findViewById<ImageView>(R.id.ivAvatar)
            if (ivAvatar != null) {
                Glide.with(this).load(it).into(ivAvatar)
            }
            profileViewModel.updateAvatar(it.toString())
            showSuccessToast(getString(R.string.msg_avatar_updated))
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!isLoggedInUseCase()) {
            showAuthRequiredDialog()
        }

        val switchDarkMode = view.findViewById<MaterialSwitch>(R.id.switchDarkMode)
        val tvLang = view.findViewById<TextView>(R.id.tvLang)
        val tvName = view.findViewById<TextView>(R.id.tvName)
        val ivAvatar = view.findViewById<ImageView>(R.id.ivAvatar)
        val tvReceiptBadge = view.findViewById<TextView>(R.id.tvReceiptBadge)
        val containerAvatar = view.findViewById<View>(R.id.containerAvatar)

        val avatarClickListener = View.OnClickListener {
            ensureAuth(isLoggedInUseCase()) {
                imagePicker.launch("image/*")
            }
        }
        containerAvatar?.setOnClickListener(avatarClickListener)
        ivAvatar?.setOnClickListener(avatarClickListener)

        // Observe Receipt Count for Badge
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                profileViewModel.receiptCount.collect { count ->
                    if (count > 0) {
                        tvReceiptBadge?.visibility = View.VISIBLE
                        tvReceiptBadge?.text = count.toString()
                    } else {
                        tvReceiptBadge?.visibility = View.GONE
                    }
                }
            }
        }

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
                        val displayName = when {
                            user.fullName.isNotBlank() -> user.fullName
                            user.nickname.isNotBlank() -> user.nickname
                            user.email.isNotBlank() -> user.email.substringBefore("@").replaceFirstChar { if (it.isLowerCase()) it.titlecase(
                                Locale.getDefault()) else it.toString() }
                            else -> getString(R.string.profile_name)
                        }
                        tvName.text = displayName
                        if (!user.avatarUrl.isNullOrEmpty()) {
                            Glide.with(this@ProfileFragment)
                                .load(user.avatarUrl)
                                .placeholder(R.drawable.bg_stylish_avatar_default)
                                .into(ivAvatar)
                        } else {
                            ivAvatar.setImageResource(R.drawable.bg_stylish_avatar_default)
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

    override fun onResume() {
        super.onResume()
        profileViewModel.loadUserProfile()
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
            ensureAuth(isLoggedInUseCase()) {
                findNavController().navigate(R.id.myBookingsFragment)
            }
        }
        view.findViewById<View>(R.id.menuMyReceipts)?.setOnClickListener {
            ensureAuth(isLoggedInUseCase()) {
                findNavController().navigate(R.id.action_profileFragment_to_receiptListFragment)
            }
        }
        view.findViewById<View>(R.id.menuPayments).setOnClickListener {
            ensureAuth(isLoggedInUseCase()) {
                findNavController().navigate(R.id.paymentsFragment)
            }
        }
        view.findViewById<View>(R.id.menuEditProfile).setOnClickListener {
            ensureAuth(isLoggedInUseCase()) {
                findNavController().navigate(R.id.editProfileFragment)
            }
        }
        view.findViewById<View>(R.id.menuSellerMode).setOnClickListener {
            ensureAuth(isLoggedInUseCase()) {
                findNavController().navigate(R.id.action_profileFragment_to_sellerDashboardFragment)
            }
        }
        view.findViewById<View>(R.id.menuNotification).setOnClickListener {
            findNavController().navigate(R.id.notificationsSettingsFragment)
        }
        view.findViewById<View>(R.id.menuSecurity).setOnClickListener {
            ensureAuth(isLoggedInUseCase()) {
                findNavController().navigate(R.id.securityFragment)
            }
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
            if (!isLoggedInUseCase()) {
                showAuthRequiredDialog()
            } else {
                LogoutBottomSheet().show(parentFragmentManager, "logout")
            }
        }
    }
}
