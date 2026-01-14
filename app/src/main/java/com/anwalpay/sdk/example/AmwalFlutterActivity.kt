package com.anwalpay.sdk.example

import android.util.Log
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.android.FlutterActivityLaunchConfigs

/**
 * Custom FlutterActivity that prevents premature finishing during 3DS flow.
 */
class AmwalFlutterActivity : FlutterActivity() {

    companion object {
        private const val TAG = "AmwalFlutterActivity"
        const val ENGINE_ID = "engine_id"
        
        @Volatile
        private var _allowFinish: Boolean = false
        
        var allowFinish: Boolean
            get() = _allowFinish
            set(value) {
                _allowFinish = value
                Log.d(TAG, "allowFinish set to: $value")
            }
    }

    override fun getCachedEngineId(): String = ENGINE_ID

    override fun finish() {
        Log.d(TAG, "finish() called, allowFinish=$allowFinish")
        if (allowFinish) {
            super.finish()
        } else {
            Log.d(TAG, "Blocking finish - 3DS may be in progress")
        }
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy()")
        _allowFinish = false
        super.onDestroy()
    }

    override fun getBackgroundMode(): FlutterActivityLaunchConfigs.BackgroundMode {
        return FlutterActivityLaunchConfigs.BackgroundMode.transparent
    }
}
