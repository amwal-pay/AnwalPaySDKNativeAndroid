# Amwal Pay SDK Integration Guide

This guide demonstrates how to integrate the Amwal Pay SDK (version 1.0.66) into your Android application.

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

Add the following dependency to your app's `build.gradle.kts`:

```kotlin
dependencies {
    implementation("com.amwal-pay:amwal_sdk:1.0.66")
}
```

## Setup

1. Add the required permissions to your `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.INTERNET"/>
```

2. Initialize the SDK in your application:

```kotlin
private val amwalSDK by lazy { AmwalSDK() }
```

## Implementation

### 1. Session Token Generation

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
    isSoftPOS = true // Set to true for NFC transactions
)
```

### 3. Starting the Payment Flow

```kotlin
amwalSDK.start(
    activity = this,
    config = config,
    onResponse = { response ->
        // Handle the payment response
        Log.d("Payment", "Response: $response")
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
- NFC
- CARD_WALLET

### Environment Support
- SIT (System Integration Testing)
- UAT (User Acceptance Testing)
- PROD (Production)

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

The SDK provides error handling through callbacks. Always implement proper error handling in your application:

```kotlin
try {
    // SDK operations
} catch (e: Exception) {
    // Handle errors appropriately
    showErrorDialog("Something Went Wrong")
}
```

## Best Practices

1. Always use the appropriate environment (SIT/UAT/PROD) for your use case
2. Implement proper error handling
3. Store sensitive data securely
4. Test thoroughly in the test environment before going to production
5. Keep the SDK updated to the latest version
6. Follow the security guidelines provided by Amwal Pay

## Support

For technical support or questions, please contact Amwal Pay support team.

---

**Note**: This documentation is based on SDK version 1.0.66. Please check for updates and new features in newer versions. 