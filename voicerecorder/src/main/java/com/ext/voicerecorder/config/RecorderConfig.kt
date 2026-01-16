package com.ext.voicerecorder.config

data class RecorderConfig(
    val fileName: String,
    val filePath: String,
    val audioSource: Int,
    val outputFormat: Int,
    val audioEncoder: Int
)