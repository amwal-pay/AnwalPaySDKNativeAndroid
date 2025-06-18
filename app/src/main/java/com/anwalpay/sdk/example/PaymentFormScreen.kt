package com.anwalpay.sdk.example

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.anwalpay.sdk.AmwalSDK
import com.anwalpay.sdk.AmwalSDK.Config.Environment
import com.anwalpay.sdk.example.ui.CustomDropdown
import com.anwalpay.sdk.example.ui.CustomTextField
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentFormScreen(
    state: PaymentFormState,
    onClick: () -> Unit,
    onDeleteCustomerId: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Amwal Pay Demo") },
                actions = {
                    IconButton(
                        onClick = {
                            onDeleteCustomerId()
                            scope.launch {
                                snackbarHostState.showSnackbar("Customer Id deleted", "dismiss")
                            }
                        }

                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Clear customer id"
                        )
                    }

                }
            )
        },
        content = { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // TextFields for input
                CustomTextField("Merchant Id", state.merchantId.value) {
                    state.merchantId.value = it
                }
                CustomTextField("Terminal Id", state.terminalId.value) {
                    state.terminalId.value = it
                }
                CustomTextField("Amount", state.amount.value) { state.amount.value = it }
                CustomTextField("Secret Key", state.secureHash.value) {
                    state.secureHash.value = it
                }

                // Dropdowns
                CustomDropdown(
                    title = "Currency",
                    options = AmwalSDK.Config.Currency.entries.map { it.name },
                    selectedValue = state.currency.value.name,
                    onValueChange = { state.currency.value = AmwalSDK.Config.Currency.valueOf(it) }
                )

                CustomDropdown(
                    title = "Language",
                    options = listOf("ar", "en"),
                    selectedValue = state.language.value,
                    onValueChange = { state.language.value = it }
                )

                CustomDropdown(
                    title = "Transaction Type",
                    options = TransactionType.entries.map { it.name },
                    selectedValue = state.transactionType.value.name,
                    onValueChange = { state.transactionType.value = TransactionType.valueOf(it) }
                )

                CustomDropdown(
                    title = "Environment",
                    options = Environment.entries.map { it.name },
                    selectedValue = state.selectedEnv.value.name,
                    onValueChange = { state.selectedEnv.value = Environment.valueOf(it) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Initiate Payment Button
                Button(
                    onClick = onClick
                ) {
                    Text("Initiate Payment Demo")
                }
            }
        }
    )
}


data class PaymentFormState(
    var merchantId: MutableState<String> = mutableStateOf("116194"),
    var terminalId: MutableState<String> = mutableStateOf("708393"),
    var amount: MutableState<String> = mutableStateOf("1"),
    var currency: MutableState<AmwalSDK.Config.Currency> = mutableStateOf(AmwalSDK.Config.Currency.OMR),
    var language: MutableState<String> = mutableStateOf("en"),
    var transactionType: MutableState<TransactionType> = mutableStateOf(TransactionType.NFC),
    var secureHash: MutableState<String> = mutableStateOf("2B03FCDC101D3F160744342BFBA0BEA0E835EE436B6A985BA30464418392C703"),
    var selectedEnv: MutableState<Environment> = mutableStateOf(Environment.UAT)
)

