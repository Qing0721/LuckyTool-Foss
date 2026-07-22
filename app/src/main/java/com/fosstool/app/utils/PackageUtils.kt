@file:Suppress("unused", "NewApi")

package com.fosstool.app.utils

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.PackageManager.ResolveInfoFlags
import android.content.pm.ResolveInfo

class PackageUtils(private val packageManager: PackageManager) {

    fun getPackageInfo(packName: String, flag: Int): PackageInfo {
        if (SDK < A13) return packageManager.getPackageInfo(packName, flag)
        return packageManager.getPackageInfo(packName, PackageManager.PackageInfoFlags.of(flag.toLong()))
    }

    fun getPackageUid(packName: String, flag: Int): Int {
        if (SDK < A13) return packageManager.getPackageUid(packName, flag)
        return packageManager.getPackageUid(packName, PackageManager.PackageInfoFlags.of(flag.toLong()))
    }

    fun getApplicationInfo(packName: String, flag: Int): ApplicationInfo {
        if (SDK < A13) return packageManager.getApplicationInfo(packName, flag)
        return packageManager.getApplicationInfo(packName, PackageManager.ApplicationInfoFlags.of(flag.toLong()))
    }

    fun getInstalledPackages(flag: Int): MutableList<PackageInfo> {
        if (SDK < A13) return packageManager.getInstalledPackages(flag)
        return packageManager.getInstalledPackages(PackageManager.PackageInfoFlags.of(flag.toLong()))
    }

    fun getInstalledApplications(flag: Int): MutableList<ApplicationInfo> {
        if (SDK < A13) return packageManager.getInstalledApplications(flag)
        return packageManager.getInstalledApplications(PackageManager.ApplicationInfoFlags.of(flag.toLong()))
    }

    fun resolveActivity(intent: Intent, flag: Int): ResolveInfo? {
        if (SDK < A13) return packageManager.resolveActivity(intent, flag)
        return packageManager.resolveActivity(intent, ResolveInfoFlags.of(flag.toLong()))
    }
}
