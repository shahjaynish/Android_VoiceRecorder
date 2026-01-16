package com.ext.voicerecorder.callback

interface VoiceRecorderCallback {
    fun onStart()
    fun onPause() {}
    fun onResume() {}
    fun onDuration(durationMs: Long) {}
    fun onStop(filePath: String)
    fun onError(error: String)
}
