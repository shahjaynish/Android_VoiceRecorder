package com.ext.voicerecorder

import android.media.MediaRecorder
import com.ext.voicerecorder.callback.VoiceRecorderCallback
import com.ext.voicerecorder.config.RecorderConfig
import com.ext.voicerecorder.internal.RecorderEngine

class VoiceRecorder(
    private val callback: VoiceRecorderCallback
) {

    private val engine = RecorderEngine()

    fun startRecording(path: String, fileName: String) {
        val config = RecorderConfig(
            fileName = fileName,
            filePath = path,
            audioSource = MediaRecorder.AudioSource.MIC,
            outputFormat = MediaRecorder.OutputFormat.MPEG_4,
            audioEncoder = MediaRecorder.AudioEncoder.AAC
        )

        engine.start(
            config = config,
            onDuration = { callback.onDuration(it) },
            onError = { callback.onError(it) }
        )

        callback.onStart()
    }

    fun stopRecording() {
        val filePath = engine.stop()
        if (filePath != null) {
            callback.onStop(filePath)
        } else {
            callback.onError("Failed to stop recording")
        }
    }

    fun pauseRecording() {
        val success = engine.pause()
        if (success.not()) {
            callback.onError("Pause not supported on this device")
        } else {
            callback.onPause()
        }
    }

    fun resumeRecording() {
        val success = engine.resume()
        if (success.not()) {
            callback.onError("Resume not supported on this device")
        } else {
            callback.onResume()
        }
    }

}
