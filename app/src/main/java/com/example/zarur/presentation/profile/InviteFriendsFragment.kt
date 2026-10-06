package com.example.zarur.presentation.profile

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.zarur.R
import com.example.zarur.util.showSuccessToast
import com.google.android.material.button.MaterialButton
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class InviteFriendsFragment : Fragment(R.layout.fragment_invite_friends) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<View>(R.id.toolbar).setOnClickListener {
            findNavController().navigateUp()
        }

        val tvReferralCode = view.findViewById<TextView>(R.id.tvReferralCode)
        val code = tvReferralCode?.text?.toString() ?: "ZARUR2026"

        val copyAction = View.OnClickListener {
            val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Referral Code", code)
            clipboard.setPrimaryClip(clip)
            showSuccessToast("Promo kod nusxalandi: $code")
        }

        view.findViewById<ImageView>(R.id.ivCopy)?.setOnClickListener(copyAction)
        tvReferralCode?.setOnClickListener(copyAction)

        view.findViewById<MaterialButton>(R.id.btnInvite)?.setOnClickListener {
            val shareText = "Zarur ilovasiga qo'shiling va eng yaxshi takliflardan foydalaning! Taklif kodi: $code\nIlovani yuklab olish: https://zarur.app"
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "Zarur App")
                putExtra(Intent.EXTRA_TEXT, shareText)
            }
            startActivity(Intent.createChooser(shareIntent, getString(R.string.title_invite_friends)))
        }
    }
}
