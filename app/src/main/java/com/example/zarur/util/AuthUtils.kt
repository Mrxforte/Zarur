package com.example.zarur.util

import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.zarur.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

fun Fragment.showAuthRequiredDialog(
    message: String? = null,
    onDismiss: (() -> Unit)? = null
) {
    MaterialAlertDialogBuilder(requireContext())
        .setTitle(getString(R.string.auth_required_title))
        .setMessage(message ?: getString(R.string.auth_required_message))
        .setPositiveButton(getString(R.string.auth_required_positive)) { dialog, _ ->
            dialog.dismiss()
            try {
                findNavController().navigate(R.id.action_global_authHubFragment)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        .setNegativeButton(getString(R.string.auth_required_negative)) { dialog, _ ->
            dialog.dismiss()
            onDismiss?.invoke()
        }
        .setOnCancelListener {
            onDismiss?.invoke()
        }
        .show()
}

fun Fragment.ensureAuth(
    isLoggedIn: Boolean,
    message: String? = null,
    onAuthed: () -> Unit
) {
    if (isLoggedIn) {
        onAuthed()
    } else {
        showAuthRequiredDialog(message)
    }
}
