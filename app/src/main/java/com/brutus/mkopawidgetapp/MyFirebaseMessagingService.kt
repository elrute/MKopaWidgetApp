// MyFirebaseMessagingService.kt
package com.brutus.mkopawidgetapp

import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCMService", "Refreshed token: $token")
        // Send token to backend for push notifications
        sendTokenToBackend(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d("FCMService", "From: ${remoteMessage.from}")

        // Check if message contains data payload
        if (remoteMessage.data.isNotEmpty()) {
            Log.d("FCMService", "Message data payload: ${remoteMessage.data}")
            handleNotification(remoteMessage.data)
        }

        // Check if message contains a notification payload
        remoteMessage.notification?.let {
            Log.d("FCMService", "Message Notification Body: ${it.body}")
            // Optionally, handle notification payload if needed
        }
    }

    private fun sendTokenToBackend(token: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val tokenRequest = TokenRequest(deviceToken = token)
                val response = BackendService.api.updateDeviceToken(tokenRequest)
                if (response.isSuccessful) {
                    Log.d("FCMService", "Token updated successfully")
                } else {
                    Log.e("FCMService", "Failed to update token: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e("FCMService", "Exception while updating token: ${e.localizedMessage}")
            }
        }
    }

    /**
     * Handles incoming notifications based on their type.
     *
     * @param data The data payload from the push notification.
     */
    private fun handleNotification(data: Map<String, String>) {
        val gson = Gson()
        val type = data["type"]

        when (type) {
            "loan_update" -> {
                handleLoanProgressUpdate(data)
            }
            "payment_outcome" -> {
                handlePaymentOutcome(data)
            }
            else -> {
                Log.w("FCMService", "Unknown notification type received")
            }
        }
    }

    /**
     * Handles the loan progress update received via push notification.
     *
     * @param data The data payload from the push notification.
     */
    private fun handleLoanProgressUpdate(data: Map<String, String>) {
        try {
            val loanProgressJson = data["loanProgress"]
            val loanProgress = loanProgressJson?.let {
                Gson().fromJson(it, LoanProgress::class.java)
            }
            loanProgress?.let {
                // Save the loan progress using PreferencesManager
                val preferencesManager = PreferencesManager.getInstance(applicationContext)
                preferencesManager.setLoanProgress(it)

                // Send broadcast to update the widget
                sendUpdateWidgetBroadcast()
            }
        } catch (e: Exception) {
            Log.e("FCMService", "Failed to parse loan progress: ${e.localizedMessage}")
        }
    }

    /**
     * Handles the payment outcome received via push notification.
     *
     * @param data The data payload from the push notification.
     */
    private fun handlePaymentOutcome(data: Map<String, String>) {
        val message = data["message"] ?: "Payment outcome received."

        // Update the payment outcome in PreferencesManager
        val preferencesManager = PreferencesManager.getInstance(applicationContext)
        preferencesManager.setPaymentOutcome(message)

        // Update the widget to reflect the payment outcome
        sendUpdateWidgetBroadcast()

    }

    /**
     * Sends a broadcast intent to update all widgets.
     */
    private fun sendUpdateWidgetBroadcast() {
        val intent = Intent("com.brutus.mkopawidgetapp.UPDATE_WIDGET")
        sendBroadcast(intent)
    }

}
