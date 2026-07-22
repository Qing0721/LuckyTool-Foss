package com.fosstool.app.data

import kotlinx.serialization.Serializable

@Serializable
data class MemcConfigPackage(
    val packName: String,
    val rate: String,
    val type: String,
)

@Serializable
data class MemcConfigActivity(
    val packName: String,
    val activity: String,
    val type: String,
)
