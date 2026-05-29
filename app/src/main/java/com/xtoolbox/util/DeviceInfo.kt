package com.xtoolbox.util

import android.os.Build

object DeviceInfo {
    val deviceModel: String
        get() = "${Build.MANUFACTURER} ${Build.MODEL}"

    val androidVersion: String
        get() = Build.VERSION.RELEASE

    val apiLevel: Int
        get() = Build.VERSION.SDK_INT

    val brand: String
        get() = Build.BRAND

    val board: String
        get() = Build.BOARD

    val fingerprint: String
        get() = Build.FINGERPRINT

    val socModel: String
        get() = Build.SOC_MODEL ?: "Unknown"
}
