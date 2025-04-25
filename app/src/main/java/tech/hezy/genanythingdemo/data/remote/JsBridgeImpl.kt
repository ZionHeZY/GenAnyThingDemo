package tech.hezy.genanythingdemo.data.remote

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.*
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import tech.hezy.genanythingdemo.domain.usecase.JsBridgeUseCase

class JsBridgeImpl(private val context: Context) : JsBridgeUseCase {
    override fun getDeviceInfo(): String =
        "Brand: ${Build.BRAND}, Model: ${Build.MODEL}, OS: Android ${Build.VERSION.RELEASE}"

    override fun vibrate(millis: Long) {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        Log.d("JsBridgeImpl", "vibrate called from JS, millis=$millis, vibrator=$vibrator, hasVibrator=${vibrator?.hasVibrator()}")
        if (vibrator != null && vibrator.hasVibrator()) {
            Handler(Looper.getMainLooper()).post {
                try {
                    vibrator.vibrate(VibrationEffect.createOneShot(millis, VibrationEffect.DEFAULT_AMPLITUDE))
                    Log.d("JsBridgeImpl", "vibrate executed on main thread")
                } catch (e: Exception) {
                    Log.e("JsBridgeImpl", "vibrate error: ${e.message}", e)
                }
            }
        } else {
            Log.e("JsBridgeImpl", "vibrator service not available or device has no vibrator")
        }
    }

    override fun showToast(message: String) {
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun openSettings() {
        val intent = Intent(Settings.ACTION_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    override fun dialPhone(number: String) {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = android.net.Uri.parse("tel:$number")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    override fun ring() {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
        audioManager?.let {
            it.ringerMode = AudioManager.RINGER_MODE_NORMAL
            it.playSoundEffect(AudioManager.FX_KEY_CLICK)
        }
    }

    override fun toggleFlashlight(on: Boolean) {
        try {
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? android.hardware.camera2.CameraManager
            val cameraId = cameraManager?.cameraIdList?.firstOrNull {
                cameraManager.getCameraCharacteristics(it).get(android.hardware.camera2.CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            }
            if (cameraId != null) {
                cameraManager.setTorchMode(cameraId, on)
            }
        } catch (e: Exception) {
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(context, "切换闪光灯失败: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
