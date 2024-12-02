// PaymentViewModel.kt
package com.brutus.mkopawidgetapp

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PaymentViewModel(application: Application) : AndroidViewModel(application) {

    // Access the application context
    private val context = getApplication<Application>().applicationContext

    // Private mutable states
    private val _loanId = MutableStateFlow("")
    val loanId: StateFlow<String> = _loanId.asStateFlow()

    private val _amount = MutableStateFlow("")
    val amount: StateFlow<String> = _amount.asStateFlow()

    private val _status = MutableStateFlow("")
    val status: StateFlow<String> = _status.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Renamed from _paymentSuccess to _paymentDone
    private val _paymentDone = MutableStateFlow(false)
    val paymentDone: StateFlow<Boolean> = _paymentDone.asStateFlow()

    fun onLoanIdChange(newLoanId: String) {
        _loanId.value = newLoanId
    }

    fun onAmountChange(newAmount: String) {
        _amount.value = newAmount
    }

    fun submitPayment() {
        // Validate inputs
        if (_loanId.value.isBlank() || _amount.value.isBlank()) {
            _status.value = "Please enter valid Loan ID and Amount."
            return
        }

        val amountDouble = _amount.value.toDoubleOrNull()
        if (amountDouble == null || amountDouble <= 0) {
            _status.value = "Please enter a valid amount."
            return
        }

        _isLoading.value = true
        _status.value = ""

        viewModelScope.launch {
            try {
                val paymentRequest = PaymentRequest(
                    loanId = _loanId.value,
                    amount = amountDouble
                )
                val response = BackendService.api.initiatePayment(paymentRequest)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val paymentResponse = response.body()
                        if (paymentResponse != null && paymentResponse.success) {
                            _status.value = "Payment Submitted Successfully."
                        } else {
                            _status.value = paymentResponse?.message ?: "Payment submission failed."
                        }
                    } else {
                        _status.value = "Payment submission failed with code: ${response.code()}"
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _status.value = "Payment submission failed: ${e.localizedMessage}"
                }
            } finally {
                withContext(Dispatchers.Main) {
                    _isLoading.value = false
                    // Indicate that the payment request has been made
                    //_paymentDone.value = true
                }
            }
        }
    }

    /**
     * Sends the device token to the backend for push notifications.
     * This can be called during app initialization or when the token is refreshed.
     */
    fun updateDeviceToken(token: String) {
        viewModelScope.launch {
            try {
                val tokenRequest = TokenRequest(deviceToken = token)
                val response = BackendService.api.updateDeviceToken(tokenRequest)
                if (response.isSuccessful) {
                    // Optionally, handle success (e.g., log it)
                } else {
                    // Optionally, handle failure (e.g., retry mechanism)
                }
            } catch (e: Exception) {
                // Optionally, handle exception (e.g., log it)
            }
        }
    }
}
