package com.example.system

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.camera2.CameraManager
import android.net.Uri
import android.os.BatteryManager
import android.provider.Settings
import com.example.data.model.Language
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SystemActionManager(private val context: Context) {

    // Common app packages on Android (especially Redmi / Xiaomi devices)
    private val appPackageMap = mapOf(
        "youtube" to "com.google.android.youtube",
        "whatsapp" to "com.whatsapp",
        "chrome" to "com.android.chrome",
        "browser" to "com.android.chrome",
        "maps" to "com.google.android.apps.maps",
        "camera" to null, // handled via media intent
        "settings" to "com.android.settings",
        "gallery" to null,
        "calculator" to null,
        "clock" to null,
        "facebook" to "com.facebook.katana",
        "messenger" to "com.facebook.orca",
        "instagram" to "com.instagram.android"
    )

    fun launchApp(query: String): ActionResult {
        val cleanQuery = query.lowercase().trim()

        // 1. Check known mappings
        val targetPackage = appPackageMap[cleanQuery]
        if (targetPackage != null) {
            val intent = context.packageManager.getLaunchIntentForPackage(targetPackage)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                return ActionResult(
                    isSuccess = true,
                    spokenFeedback = "Opening $query now, Boss.",
                    actionLabel = "Opened $cleanQuery"
                )
            }
        }

        // 2. Hardware shortcuts (Camera, Settings, Calculator, Clock)
        when (cleanQuery) {
            "camera", "कैमरा", "ক্যামেরা" -> {
                val intent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                return tryStart(intent, "Opening Camera, Boss.")
            }
            "settings", "सेटिंग्स", "সেটিংস" -> {
                val intent = Intent(Settings.ACTION_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                return tryStart(intent, "Opening System Settings, Boss.")
            }
            "wifi", "wi-fi", "वाईफाई" -> {
                val intent = Intent(Settings.ACTION_WIFI_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                return tryStart(intent, "Opening Wi-Fi settings, Boss.")
            }
            "bluetooth", "ब्लूटूथ" -> {
                val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                return tryStart(intent, "Opening Bluetooth settings, Boss.")
            }
            "battery", "बैटरी" -> {
                val intent = Intent(Intent.ACTION_POWER_USAGE_SUMMARY).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                return tryStart(intent, "Showing Battery stats, Boss.")
            }
            "clock", "alarm", "घड़ी" -> {
                val intent = Intent(android.provider.AlarmClock.ACTION_SHOW_ALARMS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                return tryStart(intent, "Opening Clock and Alarms, Boss.")
            }
        }

        // 3. Search installed applications
        try {
            val pm = context.packageManager
            val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            val apps = pm.queryIntentActivities(mainIntent, 0)
            for (app in apps) {
                val label = app.loadLabel(pm).toString().lowercase()
                if (label.contains(cleanQuery) || cleanQuery.contains(label)) {
                    val launchIntent = pm.getLaunchIntentForPackage(app.activityInfo.packageName)
                    if (launchIntent != null) {
                        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(launchIntent)
                        return ActionResult(
                            isSuccess = true,
                            spokenFeedback = "Opening ${app.loadLabel(pm)}, Boss.",
                            actionLabel = "Launched ${app.activityInfo.packageName}"
                        )
                    }
                }
            }
        } catch (e: Exception) {
            // Ignore search error
        }

        return ActionResult(
            isSuccess = false,
            spokenFeedback = "Could not locate an application named $query, Boss.",
            actionLabel = "App Not Found: $query"
        )
    }

    fun openYouTubeSearch(searchQuery: String): ActionResult {
        val query = Uri.encode(searchQuery.trim())
        val appIntent = Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:$query")).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/results?search_query=$query")).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        return try {
            context.startActivity(appIntent)
            ActionResult(true, "Searching YouTube for $searchQuery, Boss.", "YouTube Search")
        } catch (e: Exception) {
            try {
                context.startActivity(webIntent)
                ActionResult(true, "Searching YouTube for $searchQuery on browser, Boss.", "YouTube Web Search")
            } catch (err: Exception) {
                ActionResult(false, "Unable to launch YouTube, Boss.", "YouTube Launch Failed")
            }
        }
    }

    private var isFlashlightOn = false

    fun toggleFlashlight(forceState: Boolean? = null): ActionResult {
        return try {
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
            val cameraId = cameraManager?.cameraIdList?.firstOrNull()
            if (cameraManager != null && cameraId != null) {
                val targetState = forceState ?: !isFlashlightOn
                cameraManager.setTorchMode(cameraId, targetState)
                isFlashlightOn = targetState
                val stateText = if (targetState) "ON" else "OFF"
                ActionResult(true, "Flashlight turned $stateText, Boss.", "Flashlight $stateText")
            } else {
                ActionResult(false, "Flashlight hardware not available on this device, Boss.", "Torch Unavailable")
            }
        } catch (e: Exception) {
            ActionResult(false, "Torch control error: ${e.localizedMessage ?: "Device busy"}", "Torch Error")
        }
    }

    fun getCurrentDateTime(language: Language): ActionResult {
        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
        val dateFormat = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault()).format(Date())
        val spoken = when (language) {
            Language.HINDI -> "बॉस, अभी समय $timeFormat है, और आज $dateFormat है।"
            Language.BENGALI -> "বস, এখন সময় $timeFormat, এবং আজকের তারিখ $dateFormat।"
            Language.ENGLISH -> "Boss, the time is $timeFormat on $dateFormat."
        }
        return ActionResult(true, spoken, "Time & Date: $timeFormat")
    }

    fun openBrowser(url: String = "https://www.google.com"): ActionResult {
        return try {
            val targetUri = if (!url.startsWith("http://") && !url.startsWith("https://")) {
                Uri.parse("https://$url")
            } else {
                Uri.parse(url)
            }
            val intent = Intent(Intent.ACTION_VIEW, targetUri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            ActionResult(true, "Opening web browser, Boss.", "Web Browser")
        } catch (e: Exception) {
            ActionResult(false, "No browser installed on this device, Boss.", "Browser Error")
        }
    }

    fun getDeviceDiagnostics(): DeviceDiagnostics {
        val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        actManager.getMemoryInfo(memInfo)

        val totalRamMb = (memInfo.totalMem / (1024 * 1024)).toInt()
        val availRamMb = (memInfo.availMem / (1024 * 1024)).toInt()
        val isLowRam = memInfo.lowMemory || totalRamMb <= 2200

        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
        val batteryPct = batteryManager?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: -1

        return DeviceDiagnostics(
            totalRamMb = totalRamMb,
            availableRamMb = availRamMb,
            isLowMemory = isLowRam,
            batteryPercentage = batteryPct
        )
    }

    private fun tryStart(intent: Intent, successVoice: String): ActionResult {
        return try {
            context.startActivity(intent)
            ActionResult(true, successVoice, "System Intent Executed")
        } catch (e: Exception) {
            ActionResult(false, "System setting or activity unavailable on this device, Boss.", "Intent Failed")
        }
    }
}

data class ActionResult(
    val isSuccess: Boolean,
    val spokenFeedback: String,
    val actionLabel: String
)

data class DeviceDiagnostics(
    val totalRamMb: Int,
    val availableRamMb: Int,
    val isLowMemory: Boolean,
    val batteryPercentage: Int
)
