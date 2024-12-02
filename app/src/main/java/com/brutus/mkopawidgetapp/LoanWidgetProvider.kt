package com.brutus.mkopawidgetapp

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoanWidgetProvider : AppWidgetProvider() {

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == "com.brutus.mkopawidgetapp.UPDATE_WIDGET") {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, LoanWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
            for (appWidgetId in appWidgetIds) {
                updateAppWidget(context, appWidgetManager, appWidgetId)
            }
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // Perform this loop procedure for each App Widget that belongs to this provider
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }


    companion object {
        /**
         * Updates a single widget instance with the latest loan progress.
         *
         * @param context The Context in which this receiver is running.
         * @param appWidgetManager The AppWidgetManager instance.
         * @param appWidgetId The widget ID to update.
         */
        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            // Initialize RemoteViews with the widget layout
            val views = RemoteViews(context.packageName, R.layout.widget_layout)

            // Set up the intent that starts the PaymentActivity when the button is clicked
            val intent = Intent(context, PaymentActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.buttonMakePayment, pendingIntent)

            // Retrieve the latest LoanProgress using PreferencesManager
            val preferencesManager = PreferencesManager.getInstance(context)
            val loanProgress = preferencesManager.getLoanProgress()

            // Update loan progress TextView
            val progressText = loanProgress?.let {
                "Loan Progress: ${it.progressPercentage}%"
            } ?: "Loan Progress: --%"
            views.setTextViewText(R.id.textViewLoanProgress, progressText)

            // Retrieve the latest Payment Outcome using PreferencesManager
            val paymentOutcome = preferencesManager.getPaymentOutcome()
            views.setTextViewText(R.id.textViewPaymentOutcome, paymentOutcome ?: "")

            // Update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

    }

}
