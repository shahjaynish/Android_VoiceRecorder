package com.ext.voicerecorder.internal

import android.media.MediaRecorder
import android.os.Build
import android.os.Handler
import android.os.Looper
import com.ext.voicerecorder.config.RecorderConfig
import java.io.File

internal class RecorderEngine {

    private var recorder: MediaRecorder? = null
    private var startTime = 0L
    private var isPaused = false
    private var handler: Handler? = null
    private var runnable: Runnable? = null
    private var outputFilePath: String? = null


    fun start(
        config: RecorderConfig,
        onDuration: (Long) -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val directory = File(config.filePath)
            if (!directory.exists()) directory.mkdirs()

            if (!directory.canWrite()) {
                onError("Provided path is not writable")
                return
            }

            val file = File(directory, config.fileName)
            outputFilePath = file.absolutePath

            recorder = MediaRecorder().apply {
                setAudioSource(config.audioSource)
                setOutputFormat(config.outputFormat)
                setAudioEncoder(config.audioEncoder)
                setOutputFile(outputFilePath!!)
                prepare()
                start()
            }

            startTime = System.currentTimeMillis()
            startTimer(onDuration)
            isPaused = false

        } catch (e: Exception) {
            onError(e.message ?: "Recorder start failed")
        }
    }



    private fun startTimer(onDuration: (Long) -> Unit) {
        handler = Handler(Looper.getMainLooper())
        runnable = object : Runnable {
            override fun run() {
                val duration = System.currentTimeMillis() - startTime
                onDuration(duration)
                handler?.postDelayed(this, 1000)
            }
        }
        handler?.post(runnable!!)
    }

    fun pause(): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                recorder?.pause()
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    fun resume(): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                recorder?.resume()
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }


    fun stop(): String? {
        return try {
            handler?.removeCallbacks(runnable!!)
            recorder?.stop()
            recorder?.release()
            recorder = null
            outputFilePath
        } catch (e: Exception) {
            null
        }
    }
}

