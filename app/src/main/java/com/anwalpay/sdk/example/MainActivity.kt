package com.anwalpay.sdk.example

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.remember
import androidx.lifecycle.lifecycleScope
import com.anwalpay.sdk.AmwalSDK
import com.anwalpay.sdk.example.ui.colorToHex
import com.anwalpay.sdk.example.ui.theme.AnwalPaySDKExampleTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class MainActivity : ComponentActivity() {

    private val networkClient by lazy {NetworkClient(this)}
    private val amwalSDKWrapper by lazy {AmwalSDKWrapper()}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Register the activity launcher for handling back navigation from Flutter
        amwalSDKWrapper.registerActivityLauncher(this)
        
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
            Log.d("MainActivity", "Starting SDK initialization")
            LogsManager.addLog("Starting SDK initialization", LogType.INFO)
            
            val storedCustomerId = withContext(Dispatchers.IO){StorageClient.getCustomerId(this@MainActivity)}
            
            LogsManager.addLog(
                "Getting session token for merchant: ${state.merchantId.value}",
                LogType.INFO
            )
            
            val sessionToken = networkClient.fetchSessionToken(
                env = state.selectedEnv.value,
                merchantId = state.merchantId.value,
                customerId = storedCustomerId,
                secureHashValue = state.secureHash.value
            )

            // Handle the session token response
            if (sessionToken != null) {
                Log.d("MainActivity", "Session token received")
                LogsManager.addLog("Session token received, initializing SDK", LogType.INFO)
                
                val customerId = storedCustomerId

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
                    additionValues = mapOf(
                        "merchantIdentifier" to "merchant.shahd.test",
                        "primaryColor" to colorToHex(state.primaryColor.value),
                        "secondaryColor" to colorToHex(state.secondaryColor.value),
                        "ignoreReceipt" to state.ignoreReceipt.value.toString(),
                        "useBottomSheetDesign" to state.useBottomSheetDesign.value.toString()
                    ),
                    merchantReference = if (state.merchantReference.value.isNotBlank()) state.merchantReference.value else null
                )
                amwalSDKWrapper.start(this@MainActivity,config , onResponse = {
                    Log.d("MainActivity", "Response: $it")
                }, onCustomerId = {
                    // Only save customer ID if it's not null to prevent overwriting existing ID
                    if (it != null) {
                        StorageClient.saveCustomerId(this@MainActivity, it)
                        Log.d("MainActivity", "Customer ID saved: $it")
                    } else {
                        Log.d("MainActivity", "Customer ID is null, keeping existing ID")
                    }
                })
            } else {
                Log.e("MainActivity", "Failed to retrieve session token")
            }
        }
    }
}
