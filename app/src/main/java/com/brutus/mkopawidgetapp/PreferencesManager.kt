// PreferencesManager.kt
package com.brutus.mkopawidgetapp

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson

/**
 * Singleton class to manage SharedPreferences operations.
 */
class PreferencesManager private constructor(context: Context) {

    companion object {
        @Volatile
        private var INSTANCE: PreferencesManager? = null

        /**
         * Retrieves the singleton instance of PreferencesManager.
         *
         * @param context The application context.
         * @return The PreferencesManager instance.
         */
        fun getInstance(context: Context): PreferencesManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: PreferencesManager(context.applicationContext).also { INSTANCE = it }
            }
        }

        private const val PREFS_NAME = "MKopaPrefs"
        private const val KEY_LOAN_PROGRESS = "loan_progress"
        private const val KEY_PAYMENT_OUTCOME = "payment_outcome"
    }

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    /**
     * Saves the LoanProgress object to SharedPreferences.
     *
     * @param loanProgress The LoanProgress object to save.
     */
    fun setLoanProgress(loanProgress: LoanProgress) {
        val json = gson.toJson(loanProgress)
        sharedPreferences.edit().putString(KEY_LOAN_PROGRESS, json).apply()
    }

    /**
     * Retrieves the latest LoanProgress object from SharedPreferences.
     *
     * @return The LoanProgress object or null if not found.
     */
    fun getLoanProgress(): LoanProgress? {
        val json = sharedPreferences.getString(KEY_LOAN_PROGRESS, null)
        return json?.let {
            gson.fromJson(it, LoanProgress::class.java)
        }
    }

    /**
     * Saves the payment outcome message to SharedPreferences.
     *
     * @param message The payment outcome message to save.
     */
    fun setPaymentOutcome(message: String) {
        sharedPreferences.edit().putString(KEY_PAYMENT_OUTCOME, message).apply()
    }

    /**
     * Retrieves the payment outcome message from SharedPreferences.
     *
     * @return The payment outcome message or null if not found.
     */
    fun getPaymentOutcome(): String? {
        return sharedPreferences.getString(KEY_PAYMENT_OUTCOME, null)
    }
}
