package com.anwalpay.sdk.example

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.lifecycleScope
import com.anwalpay.sdk.AmwalSDK
import com.anwalpay.sdk.example.ui.theme.AnwalPaySDKExampleTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class MainActivity : ComponentActivity() {

    private val networkClient by lazy {NetworkClient(this)}
    private val amwalSDK by lazy {AmwalSDK()}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AnwalPaySDKExampleTheme {
                val state = remember { PaymentFormState() }

                PaymentFormScreen(
                    state,
                    onClick = {runSdk(state)},
                    onDeleteCustomerId = {
                        StorageClient.removeCustomerId(this@MainActivity)
                    }
                );
            }
        }
    }


    private fun runSdk(state: PaymentFormState) {
        lifecycleScope.launch {
            val sessionToken = networkClient.fetchSessionToken(
                env = state.selectedEnv.value,
                merchantId = state.merchantId.value,
                customerId = null,
                secureHashValue = state.secureHash.value
            )

            // Handle the session token response
            if (sessionToken != null) {
                Log.d("MainActivity", "Session Token: $sessionToken")
                val customerId = withContext(Dispatchers.IO){StorageClient.getCustomerId(this@MainActivity)}
                val config = AmwalSDK.Config (
                    environment = state.selectedEnv.value,
                    sessionToken = sessionToken,
                    currency = state.currency.value,
                    amount = state.amount.value,
                    merchantId = state.merchantId.value,
                    terminalId = state.terminalId.value,
                    locale = Locale(state.language.value),
                    customerId = customerId,
                    transactionType = when (state.transactionType.value) {
                        TransactionType.NFC -> AmwalSDK.Config.TransactionType.NFC
                        TransactionType.CARD_WALLET -> AmwalSDK.Config.TransactionType.CARD_WALLET
                        TransactionType.GOOGLE_PAY -> AmwalSDK.Config.TransactionType.GOOGLE_PAY
                    },
                    transactionId = AmwalSDK.Config.generateTransactionId(), // Optional: Can be null for auto-generation
                    additionValues = AmwalSDK.Config.generateDefaultAdditionValues() // Optional: Includes merchantIdentifier for Apple Pay
                )
                amwalSDK.start(this@MainActivity,config , onResponse = {
                    Log.d("MainActivity", "Response: $it")
                }, onCustomerId = {
                    StorageClient.saveCustomerId(this@MainActivity,it)
                })
            } else {
                Log.e("MainActivity", "Failed to retrieve session token")
            }
        }
    }
}


