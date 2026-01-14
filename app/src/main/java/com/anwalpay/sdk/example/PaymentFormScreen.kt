package com.anwalpay.sdk.example

import android.R.attr.onClick
import android.icu.number.Precision.currency
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable;
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.anwalpay.sdk.example.ui.ColorPickerRow
import com.anwalpay.sdk.example.ui.CustomDropdown
import com.anwalpay.sdk.example.ui.CustomTextField
import com.anwalpay.sdk.example.ui.CustomToggle
import com.anwalpay.sdk.example.ui.colorToHex
import com.anwalpay.sdk.example.ui.hexToColor
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.anwalpay.sdk.AmwalSDK
import com.anwalpay.sdk.AmwalSDK.Config.Environment

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentFormScreen(state: PaymentFormState,onClick: () -> Unit, onDeleteCustomerId: ()-> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Amwal Pay Demo") },
                actions = {
                    IconButton(onClick =  onDeleteCustomerId) {
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
                CustomTextField("Merchant Id", state.merchantId.value) { state.merchantId.value = it }
                CustomTextField("Terminal Id", state.terminalId.value) { state.terminalId.value = it }
                CustomTextField("Amount", state.amount.value) { state.amount.value = it }
                CustomTextField("Secret Key", state.secureHash.value) { state.secureHash.value = it }
                CustomTextField("Merchant Reference (Optional)", state.merchantReference.value) { state.merchantReference.value = it }

                // Dropdowns
                CustomDropdown(
                    title = "Currency",
                    options = AmwalSDK.Config.Currency.entries.map { it.name },
                    selectedValue = state.currency.value.name,
                    onValueChange = { state.currency.value = AmwalSDK.Config.Currency.valueOf(it)  }
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

                // Color pickers
                ColorPickerRow(
                    title = "Primary Color",
                    selectedColor = state.primaryColor.value,
                    onColorSelected = { color -> state.primaryColor.value = color }
                )

                ColorPickerRow(
                    title = "Secondary Color",
                    selectedColor = state.secondaryColor.value,
                    onColorSelected = { color -> state.secondaryColor.value = color }
                )

                // Ignore Receipt Toggle
                CustomToggle(
                    title = "Ignore Receipt",
                    isChecked = state.ignoreReceipt.value,
                    onCheckedChange = { state.ignoreReceipt.value = it }
                )

                // Use Bottom Sheet Design Toggle
                CustomToggle(
                    title = "Use Bottom Sheet Design",
                    isChecked = state.useBottomSheetDesign.value,
                    onCheckedChange = { state.useBottomSheetDesign.value = it }
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
    var transactionType: MutableState<TransactionType> = mutableStateOf(TransactionType.CARD_WALLET),
    var secureHash: MutableState<String> = mutableStateOf("2B03FCDC101D3F160744342BFBA0BEA0E835EE436B6A985BA30464418392C703"),
    var selectedEnv: MutableState<Environment> = mutableStateOf(Environment.UAT),
    var merchantReference: MutableState<String> = mutableStateOf("1234"),
    var primaryColor: MutableState<Color> = mutableStateOf(hexToColor("#7F22FF")),
    var secondaryColor: MutableState<Color> = mutableStateOf(hexToColor("#37658c")),
    var ignoreReceipt: MutableState<Boolean> = mutableStateOf(false),
    var useBottomSheetDesign: MutableState<Boolean> = mutableStateOf(false)
)
