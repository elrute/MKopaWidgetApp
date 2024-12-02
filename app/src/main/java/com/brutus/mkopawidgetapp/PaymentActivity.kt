// PaymentActivity.kt
package com.brutus.mkopawidgetapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModelProvider
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import com.brutus.mkopawidgetapp.ui.theme.MKopaWidgetAppTheme

class PaymentActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Obtain the ViewModel without a factory
        val paymentViewModel: PaymentViewModel = ViewModelProvider(this).get(PaymentViewModel::class.java)

        setContent {
            MKopaWidgetAppTheme {
                // Pass the ViewModel to the PaymentScreen composable
                PaymentScreen(paymentViewModel)
            }
        }

        // Observe the paymentDone state and close the Activity when true
        lifecycleScope.launchWhenStarted {
            paymentViewModel.paymentDone.collect { done ->
                if (done) {
                    // Optionally, display a toast or message
                    // Toast.makeText(this@PaymentActivity, "Payment Submitted Successfully!", Toast.LENGTH_SHORT).show()
                    finish() // Close the Activity
                }
            }
        }
    }
}

@Composable
fun PaymentScreen(paymentViewModel: PaymentViewModel) {
    // Observe the ViewModel's state
    val loanId by paymentViewModel.loanId.collectAsState()
    val amount by paymentViewModel.amount.collectAsState()
    val status by paymentViewModel.status.collectAsState()
    val isLoading by paymentViewModel.isLoading.collectAsState()

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.Top
    ) {
        Text(text = "Make a Payment", style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = loanId,
            onValueChange = { paymentViewModel.onLoanIdChange(it) },
            label = { Text("Loan ID") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = amount,
            onValueChange = { paymentViewModel.onAmountChange(it) },
            label = { Text("Amount") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Number
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                paymentViewModel.submitPayment()
            },
            modifier = Modifier.align(Alignment.End),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(24.dp)
                        .padding(end = 8.dp),
                    strokeWidth = 2.dp
                )
                Text("Processing...")
            } else {
                Text("Submit Payment")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = status,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PaymentScreenPreview() {
    // For preview purposes, you can create a dummy ViewModel or pass null
    // Here, we're skipping ViewModel integration in the preview
    MKopaWidgetAppTheme {
        // Create a dummy ViewModel with default values
        // Note: Accessing application context in a preview might not be straightforward
        // Consider mocking or simplifying for previews
    }
}
