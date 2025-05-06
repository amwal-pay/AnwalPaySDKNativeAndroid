package com.anwalpay.sdk.example;

import android.content.Context
import android.util.Log
import com.anwalpay.sdk.AmwalSDK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class NetworkClient(private val context: Context) {
    private val TAG = "NetworkClient"
    private val client = OkHttpClient()

    suspend fun fetchSessionToken(
        env: AmwalSDK.Config.Environment,
        merchantId: String,
        customerId: String?,
        secureHashValue: String
    ): String? {
        Log.d(TAG, "Starting fetchSessionToken with env: $env, merchantId: $merchantId, customerId: $customerId")
        
        val webhookUrl = when (env) {
            AmwalSDK.Config.Environment.SIT -> "https://test.amwalpg.com:24443/"
            AmwalSDK.Config.Environment.UAT -> "https://test.amwalpg.com:14443/"
            AmwalSDK.Config.Environment.PROD -> "https://webhook.amwalpg.com/"
        }
        Log.d(TAG, "Selected webhook URL: $webhookUrl")

        return withContext(Dispatchers.IO) {
            try {
                val dataMap = mutableMapOf(
                    "merchantId" to merchantId,
                    "customerId" to customerId
                )
                Log.d(TAG, "Created data map: $dataMap")

                val secureHash = SecureHashUtil.clearSecureHash(secureHashValue, dataMap)
                Log.d(TAG, "Generated secure hash")

                val jsonBody = JSONObject().apply {
                    put("merchantId", merchantId)
                    put("secureHashValue", secureHash)
                    put("customerId", customerId)
                }
                Log.d(TAG, "Request body: ${jsonBody.toString()}")

                val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaTypeOrNull())

                val request = Request.Builder()
                    .url("${webhookUrl}Membership/GetSDKSessionToken")
                    .header("accept", "text/plain")
                    .header("accept-language", "en-US,en;q=0.9")
                    .header("content-type", "application/json")
                    .post(requestBody)
                    .build()
                Log.d(TAG, "Request URL: ${request.url}")
                Log.d(TAG, "Request headers: ${request.headers}")

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()
                Log.d(TAG, "Response code: ${response.code}")
                Log.d(TAG, "Response body: $responseBody")

                if (response.isSuccessful && responseBody != null) {
                    val jsonResponse = JSONObject(responseBody)
                    if (jsonResponse.optBoolean("success")) {
                        val sessionToken = jsonResponse.getJSONObject("data").getString("sessionToken")
                        Log.d(TAG, "Successfully retrieved session token")
                        return@withContext sessionToken
                    } else {
                        Log.e(TAG, "API returned success=false in response")
                    }
                } else {
                    val errorMessage = JSONObject(responseBody ?: "{}").optJSONArray("errorList")?.join(",") ?: "Unknown error"
                    Log.e(TAG, "API request failed: $errorMessage")
                    showErrorDialog(errorMessage)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception occurred: ${e.message}", e)
                showErrorDialog("Something Went Wrong")
            }
            return@withContext null
        }
    }

    private suspend fun showErrorDialog(message: String) {
        Log.d(TAG, "Showing error dialog with message: $message")
        withContext(Dispatchers.Main) {
            android.app.AlertDialog.Builder(context)
                .setTitle("Error")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show()
        }
    }
}
