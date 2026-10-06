package com.example.zarur.util

import android.app.Activity
import android.graphics.Color
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.zarur.R
import com.google.android.material.card.MaterialCardView
import com.google.android.material.snackbar.Snackbar

enum class ToastType {
    ERROR, SUCCESS, INFO
}

object ToastUtils {

    fun showCustomToast(
        view: View,
        message: String,
        type: ToastType = ToastType.ERROR,
        title: String? = null,
        duration: Int = Snackbar.LENGTH_LONG
    ) {
        val rootLayout = findSuitableParent(view) ?: return
        val snackbar = Snackbar.make(rootLayout, "", duration)

        val snackbarView = snackbar.view
        snackbarView.setBackgroundColor(Color.TRANSPARENT)
        snackbarView.setPadding(0, 0, 0, 0)

        val customView = LayoutInflater.from(view.context).inflate(R.layout.layout_custom_toast, null)

        val cardToast = customView.findViewById<MaterialCardView>(R.id.cardToast)
        val flIconContainer = customView.findViewById<FrameLayout>(R.id.flIconContainer)
        val ivToastIcon = customView.findViewById<ImageView>(R.id.ivToastIcon)
        val tvToastTitle = customView.findViewById<TextView>(R.id.tvToastTitle)
        val tvToastMessage = customView.findViewById<TextView>(R.id.tvToastMessage)
        val ivClose = customView.findViewById<ImageView>(R.id.ivClose)

        val context = view.context

        when (type) {
            ToastType.ERROR -> {
                cardToast.strokeColor = ContextCompat.getColor(context, R.color.red_uzum)
                flIconContainer.backgroundTintList = ContextCompat.getColorStateList(context, R.color.red_uzum)?.withAlpha(30)
                ivToastIcon.setImageResource(R.drawable.ic_toast_error)
                ivToastIcon.imageTintList = ContextCompat.getColorStateList(context, R.color.red_uzum)
                tvToastTitle.text = title ?: context.getString(R.string.title_error)
            }
            ToastType.SUCCESS -> {
                cardToast.strokeColor = ContextCompat.getColor(context, R.color.green_uzum)
                flIconContainer.backgroundTintList = ContextCompat.getColorStateList(context, R.color.green_uzum)?.withAlpha(30)
                ivToastIcon.setImageResource(R.drawable.ic_toast_success)
                ivToastIcon.imageTintList = ContextCompat.getColorStateList(context, R.color.green_uzum)
                tvToastTitle.text = title ?: context.getString(R.string.title_success)
            }
            ToastType.INFO -> {
                cardToast.strokeColor = ContextCompat.getColor(context, R.color.uzum_purple)
                flIconContainer.backgroundTintList = ContextCompat.getColorStateList(context, R.color.uzum_purple)?.withAlpha(30)
                ivToastIcon.setImageResource(R.drawable.ic_toast_info)
                ivToastIcon.imageTintList = ContextCompat.getColorStateList(context, R.color.uzum_purple)
                tvToastTitle.text = title ?: context.getString(R.string.title_info)
            }
        }

        tvToastMessage.text = message

        ivClose.setOnClickListener {
            snackbar.dismiss()
        }

        val layoutParams = snackbarView.layoutParams as? FrameLayout.LayoutParams
        layoutParams?.let {
            it.gravity = Gravity.TOP
            it.setMargins(0, 48, 0, 0)
            snackbarView.layoutParams = it
        }

        val snackbarGroup = snackbarView as? ViewGroup
        snackbarGroup?.removeAllViews()
        snackbarGroup?.addView(customView)
        snackbar.show()
    }

    private fun findSuitableParent(view: View): ViewGroup? {
        var currentView: View? = view
        var fallback: ViewGroup? = null
        do {
            if (currentView is CoordinatorLayout) {
                return currentView
            } else if (currentView is FrameLayout) {
                if (currentView.id == android.R.id.content) {
                    return currentView
                } else {
                    fallback = currentView
                }
            }
            if (currentView != null) {
                val parent = currentView.parent
                currentView = if (parent is View) parent else null
            }
        } while (currentView != null)
        return fallback
    }
}

fun Fragment.showErrorToast(message: String, title: String? = null) {
    view?.let {
        ToastUtils.showCustomToast(it, message, ToastType.ERROR, title)
    }
}

fun Fragment.showSuccessToast(message: String, title: String? = null) {
    view?.let {
        ToastUtils.showCustomToast(it, message, ToastType.SUCCESS, title)
    }
}

fun Fragment.showInfoToast(message: String, title: String? = null) {
    view?.let {
        ToastUtils.showCustomToast(it, message, ToastType.INFO, title)
    }
}

fun Activity.showErrorToast(message: String, title: String? = null) {
    findViewById<View>(android.R.id.content)?.let {
        ToastUtils.showCustomToast(it, message, ToastType.ERROR, title)
    }
}

fun Activity.showSuccessToast(message: String, title: String? = null) {
    findViewById<View>(android.R.id.content)?.let {
        ToastUtils.showCustomToast(it, message, ToastType.SUCCESS, title)
    }
}

fun Activity.showInfoToast(message: String, title: String? = null) {
    findViewById<View>(android.R.id.content)?.let {
        ToastUtils.showCustomToast(it, message, ToastType.INFO, title)
    }
}
