package com.fosstool.app.ui.fragment

data class SortMeta(
    val packName: String,
    val appName: String,
    val size: Long,
    val installTime: Long,
    val updateTime: Long,
    val targetSdk: Int,
)
