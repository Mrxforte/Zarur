package com.example.zarur.presentation.service

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.zarur.data.local.AppPreferences
import java.util.concurrent.TimeUnit

object WelcomeNotificationScheduler {

    fun scheduleWelcomeNotification(context: Context, appPreferences: AppPreferences) {
        if (!appPreferences.hasReceivedWelcomeNotification) {
            appPreferences.hasReceivedWelcomeNotification = true

            val workRequest = OneTimeWorkRequestBuilder<WelcomeNotificationWorker>()
                .setInitialDelay(1, TimeUnit.MINUTES)
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                "welcome_notification_work",
                ExistingWorkPolicy.KEEP,
                workRequest
            )
        }
    }
}
