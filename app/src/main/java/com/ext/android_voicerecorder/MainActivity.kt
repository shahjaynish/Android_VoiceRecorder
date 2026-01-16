package com.ext.android_voicerecorder

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.ext.android_voicerecorder.databinding.ActivityMainBinding
import com.ext.voicerecorder.VoiceRecorder
import com.ext.voicerecorder.callback.VoiceRecorderCallback
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var recorder: VoiceRecorder
    private lateinit var binding: ActivityMainBinding
    private var lastRecordingPath: String? = null
    private var mediaPlayer: MediaPlayer? = null

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                startRecording()
            } else {
                Toast.makeText(this, "Mic permission denied", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        recorder = VoiceRecorder(object : VoiceRecorderCallback {

            override fun onStart() {
                binding.tvTimer.text = "00:00"
                Log.d("VoiceRecorder", "Recording started")
            }

            override fun onDuration(durationMs: Long) {
                runOnUiThread {
                    val totalSeconds = durationMs / 1000
                    val minutes = totalSeconds / 60
                    val seconds = totalSeconds % 60
                    binding.tvTimer.text =
                        String.format("%02d:%02d", minutes, seconds)
                }
            }

            override fun onStop(filePath: String) {
                lastRecordingPath = filePath
                Log.d("RecorderPath", "Saved at: $filePath")
            }

            override fun onError(error: String) {
                Toast.makeText(this@MainActivity, error, Toast.LENGTH_SHORT).show()
            }
        })


        binding.btnStart.setOnClickListener { checkMicPermission() }
        binding.btnPause.setOnClickListener { recorder.pauseRecording() }
        binding.btnResume.setOnClickListener { recorder.resumeRecording() }
        binding.btnStop.setOnClickListener { recorder.stopRecording() }

        binding.btnPlay.setOnClickListener {
            lastRecordingPath?.let {
                playRecording(it)
            } ?: Toast.makeText(this, "No recording found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkMicPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            startRecording()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    private fun startRecording() {
        recorder.startRecording(
            path = filesDir.absolutePath,
            fileName = "voice_${System.currentTimeMillis()}.mp4"
        )
    }

    private fun playRecording(path: String) {
        try {
            val file = File(path)

            if (!file.exists() || file.length() == 0L) {
                Toast.makeText(this, "Recording file not found", Toast.LENGTH_SHORT).show()
                return
            }

            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setDataSource(file.absolutePath)
                prepare()
                start()
            }

            Toast.makeText(this, "Playing recording", Toast.LENGTH_SHORT).show()

        } catch (e: Exception) {
            Log.e("Playback", "Error", e)
            Toast.makeText(this, "Playback failed", Toast.LENGTH_SHORT).show()
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
    }
}
