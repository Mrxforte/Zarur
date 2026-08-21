package com.example.zarur.presentation.profile

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.zarur.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileFragment : Fragment(R.layout.fragment_profile) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<View>(R.id.menuMyBooking).setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_myBookingsFragment)
        }

        view.findViewById<View>(R.id.menuPayments).setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_paymentsFragment)
        }

        view.findViewById<View>(R.id.menuEditProfile).setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_editProfileFragment)
        }

        view.findViewById<View>(R.id.menuNotification).setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_notificationsSettingsFragment)
        }

        view.findViewById<View>(R.id.menuSecurity).setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_securityFragment)
        }

        view.findViewById<View>(R.id.menuLanguage).setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_languageFragment)
        }

        view.findViewById<View>(R.id.menuHelpCenter).setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_helpCenterFragment)
        }

        view.findViewById<View>(R.id.menuInviteFriends).setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_inviteFriendsFragment)
        }

        view.findViewById<View>(R.id.menuLogout).setOnClickListener {
            LogoutBottomSheet().show(parentFragmentManager, "logout")
        }
    }
}
