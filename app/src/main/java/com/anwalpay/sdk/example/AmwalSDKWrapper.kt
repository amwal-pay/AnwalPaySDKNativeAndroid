package com.anwalpay.sdk.example

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.anwalpay.sdk.AmwalSDK
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.embedding.engine.FlutterEngineCache
import io.flutter.embedding.engine.dart.DartExecutor
import io.flutter.plugin.common.MethodChannel

/**
 * Wrapper around AmwalSDK that uses custom AmwalFlutterActivity
 * to prevent premature finishing during 3DS flow.
 */
class AmwalSDKWrapper {

    companion object {
        const val ENGINE_ID = "engine_id"
        const val CHANNEL = "amwal.sdk/functions"
        private const val TAG = "AmwalSDKWrapper"
    }

    private var isEngineInitialized: Boolean = false
    private lateinit var flutterEngine: FlutterEngine
    private var activityLauncher: ActivityResultLauncher<Intent>? = null
    private var pendingOnResponse: ((String?) -> Unit)? = null
    private var hasReceivedResponse: Boolean = false

    fun registerActivityLauncher(activity: ComponentActivity) {
        activityLauncher = activity.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            Log.d(TAG, "Activity result received: resultCode=${result.resultCode}")
            if (!hasReceivedResponse) {
                Log.d(TAG, "No response received from Flutter, treating as cancelled")
                pendingOnResponse?.invoke(null)
            }
            hasReceivedResponse = false
            pendingOnResponse = null
        }
    }

    fun start(
        context: Context,
        config: AmwalSDK.Config,
        onResponse: (String?) -> Unit,
        onCustomerId: (String?) -> Unit
    ) {
        Log.d(TAG, "Starting AmwalSDKWrapper")

        try {
            if (isEngineInitialized) {
                Log.d(TAG, "Destroying existing engine")
                destroy()
            }

            hasReceivedResponse = false
            pendingOnResponse = onResponse

            Log.d(TAG, "Warming up engine")
            warmupEngine(context, onResponse, onCustomerId)
            isEngineInitialized = true

            Log.d(TAG, "Executing Dart entrypoint")
            flutterEngine.dartExecutor.executeDartEntrypoint(
                DartExecutor.DartEntrypoint.createDefault(), listOf(config.toJsonString())
            )

            // Reset finish flag before starting
            AmwalFlutterActivity.allowFinish = false
            
            // Use custom AmwalFlutterActivity
            val intent = Intent(context, AmwalFlutterActivity::class.java)

            Log.d(TAG, "Starting AmwalFlutterActivity")
            context.startActivity(intent)
            Log.d(TAG, "AmwalFlutterActivity started")
        } catch (e: Exception) {
            Log.e(TAG, "Error in start: ${e.message}", e)
            throw e
        }
    }

    private fun warmupEngine(
        context: Context,
        onResponse: (String?) -> Unit,
        onCustomerId: (String?) -> Unit
    ) {
        try {
            flutterEngine = FlutterEngine(context)
            Log.d(TAG, "FlutterEngine created")

            MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL)
                .setMethodCallHandler { call, result ->
                    Log.d(TAG, "Method channel call: ${call.method}")
                    try {
                        when (call.method) {
                            "onResponse" -> {
                                val response = call.argument<String?>("response")
                                Log.d(TAG, "onResponse: $response")
                                hasReceivedResponse = true
                                AmwalFlutterActivity.allowFinish = true
                                onResponse(response)
                                result.success(0)
                            }
                            "onCustomerId" -> {
                                val customerId = call.argument<String?>("customerId")
                                Log.d(TAG, "onCustomerId: $customerId")
                                onCustomerId(customerId)
                                result.success(0)
                            }
                            else -> {
                                result.notImplemented()
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error: ${e.message}", e)
                        result.error("ERROR", e.message, null)
                    }
                }

            FlutterEngineCache.getInstance().put(ENGINE_ID, flutterEngine)
            Log.d(TAG, "FlutterEngine cached")
        } catch (e: Exception) {
            Log.e(TAG, "Error in warmupEngine: ${e.message}", e)
            throw e
        }
    }

    fun destroy() {
        try {
            if (isEngineInitialized) {
                FlutterEngineCache.getInstance().remove(ENGINE_ID)
                flutterEngine.destroy()
                isEngineInitialized = false
                Log.d(TAG, "FlutterEngine destroyed")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in destroy: ${e.message}", e)
        }
    }
}
