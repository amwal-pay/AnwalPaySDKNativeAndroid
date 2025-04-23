# Amwal Pay SDK Integration Guide

This guide demonstrates how to integrate the Amwal Pay SDK (version 1.0.70) into your Android application.

## Table of Contents
- [Prerequisites](#prerequisites)
- [Dependencies](#dependencies)
- [Setup](#setup)
- [Implementation](#implementation)
- [Configuration](#configuration)
- [Usage Example](#usage-example)
- [Transaction Types](#transaction-types)
- [Environment Support](#environment-support)
- [Security](#security)

## Prerequisites

- Android Studio Arctic Fox or newer
- Minimum SDK version: 24
- Target SDK version: 35
- Kotlin version: 2.0.0 or higher
- Gradle version: 8.8.0 or higher

## Dependencies

1. Configure your project's `settings.gradle.kts` with the required repositories:

```kotlin
pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
        maven {
            url = uri("https://storage.googleapis.com/download.flutter.io")
        }
    }
}
```

2. Add the following dependency to your app's `build.gradle.kts`:

```kotlin
dependencies {
    implementation("com.amwal-pay:amwal_sdk:1.0.70")
}
```

## Setup

1. Add the required permissions to your `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.INTERNET"/>
<uses-permission android:name="android.permission.NFC"/>
<uses-feature android:name="android.hardware.nfc" android:required="true"/>
```

2. Initialize the SDK in your application:

```kotlin
private val amwalSDK by lazy { AmwalSDK() }
```

## Implementation

### 1. Session Token Generation (Must do it from backend side)

First, you need to generate a session token using the following code:

```kotlin
val sessionToken = networkClient.fetchSessionToken(
    env = AmwalSDK.Config.Environment.UAT, // or SIT, PROD
    merchantId = "YOUR_MERCHANT_ID",
    customerId = null, // Optional
    secureHashValue = "YOUR_SECURE_HASH"
)
```

### 2. SDK Configuration

Configure the SDK with the required parameters:

```kotlin
val config = AmwalSDK.Config(
    environment = AmwalSDK.Config.Environment.UAT, // or SIT, PROD
    sessionToken = sessionToken,
    currency = AmwalSDK.Config.Currency.OMR, // or other supported currencies
    amount = "1.00",
    merchantId = "YOUR_MERCHANT_ID",
    terminalId = "YOUR_TERMINAL_ID",
    locale = Locale("en"), // or "ar" for Arabic
    customerId = customerId, // Optional
    transactionType = AmwalSDK.Config.TransactionType.NFC // For NFC transactions
)
```

### 3. Starting the Payment Flow

```kotlin
amwalSDK.start(
    activity = this,
    config = config,
    onResponse = { response ->
        // Handle the payment response
        when (response) {
            is AmwalSDK.Response.Success -> {
                Log.d("Payment", "Transaction successful: ${response.transactionId}")
            }
            is AmwalSDK.Response.Error -> {
                Log.e("Payment", "Transaction failed: ${response.message}")
            }
            is AmwalSDK.Response.Cancelled -> {
                Log.d("Payment", "Transaction cancelled by user")
            }
        }
    },
    onCustomerId = { customerId ->
        // Handle the customer ID
        StorageClient.saveCustomerId(context, customerId)
    }
)
```

## Configuration

### Supported Currencies
- OMR (Omani Rial)

### Transaction Types
- NFC (Near Field Communication)
  - Use `TransactionType.NFC` for NFC transactions
  - Requires NFC hardware support
  - Requires NFC permission in manifest
- CARD_WALLET
  - Use `TransactionType.CARD_WALLET` for digital wallet transactions
  - Card-based payments
- GOOGLE_PAY
  - Use `TransactionType.GOOGLE_PAY` for Google Pay transactions
  - Requires Google Pay setup

### Environment Support
- SIT (System Integration Testing)
  - Use for initial development and testing
  - Test environment with mock data
- UAT (User Acceptance Testing)
  - Use for pre-production testing
  - Real environment with test data
- PROD (Production)
  - Use for live transactions
  - Real environment with real data

## Security

The SDK implements secure hash generation for API requests. Use the `SecureHashUtil` class to generate secure hashes:

```kotlin
val secureHash = SecureHashUtil.clearSecureHash(
    secretKey = "YOUR_SECRET_KEY",
    data = mutableMapOf(
        "merchantId" to merchantId,
        "customerId" to customerId
    )
)
```

## Error Handling

The SDK provides comprehensive error handling through callbacks. Always implement proper error handling in your application:

```kotlin
try {
    // SDK operations
} catch (e: AmwalSDKException) {
    when (e) {
        is AmwalSDKException.NetworkError -> {
            // Handle network-related errors
            showErrorDialog("Network connection error")
        }
        is AmwalSDKException.InvalidConfiguration -> {
            // Handle configuration errors
            showErrorDialog("Invalid configuration")
        }
        is AmwalSDKException.NFCNotAvailable -> {
            // Handle NFC-related errors
            showErrorDialog("NFC is not available")
        }
        else -> {
            // Handle other errors
            showErrorDialog("An unexpected error occurred")
        }
    }
}
```

## Best Practices

1. Always use the appropriate environment (SIT/UAT/PROD) for your use case
2. Use the correct TransactionType for your payment method
3. Implement proper error handling for all possible scenarios
4. Store sensitive data securely using Android's security best practices
5. Test thoroughly in the test environment before going to production
6. Keep the SDK updated to the latest version
7. Follow the security guidelines provided by Amwal Pay
8. Check for NFC availability before initiating NFC transactions
9. Handle configuration changes and lifecycle events properly

## Support

For technical support or questions, please contact Amwal Pay support team at support@amwal-pay.com

---

**Note**: This documentation is based on SDK version 1.0.70. Please check for updates and new features in newer versions. 