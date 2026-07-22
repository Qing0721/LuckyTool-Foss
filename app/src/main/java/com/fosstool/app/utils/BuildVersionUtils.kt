@file:Suppress("unused")

package com.fosstool.app.utils

import android.os.Build
import com.fosstool.app.BuildConfig
import com.fosstool.app.hook.utils.OplusBuildUtlils

val SDK get() = Build.VERSION.SDK_INT

val A11 get() = Build.VERSION_CODES.R

val A12 get() = Build.VERSION_CODES.S

val A121 get() = Build.VERSION_CODES.S_V2

val A13 get() = Build.VERSION_CODES.TIRAMISU

val A14 get() = Build.VERSION_CODES.UPSIDE_DOWN_CAKE

val A15 get() = if (Build.VERSION.SDK_INT >= 34) 35 else 0

val A16 get() = if (Build.VERSION.SDK_INT >= 35) 36 else 0

val getVersionName get() = BuildConfig.VERSION_NAME
val getVersionCode get() = BuildConfig.VERSION_CODE

val getOSVersionName get() = safeOf("null") { OplusBuildUtlils().getOSVersionName ?: "null" }

val getOSVersionCode get() = safeOf(0) { OplusBuildUtlils().getOSVersionCode ?: 0 }
