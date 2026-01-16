## Android Voice Recorder Library (Kotlin)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9-blue?logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![License: MIT](https://img.shields.io/badge/License-MIT-green)](LICENSE)
[![API](https://img.shields.io/badge/API-24%2B-orange)](#)
---

A lightweight, permission-safe Android Kotlin library for recording voice audio with start / pause / resume / stop, recording duration callback, and full control over storage location.

---

### Features

- Start voice recording using device microphone
- Pause & ▶ Resume recording (API 24+)
- Stop recording safely
- Live recording duration callback
- Play recorded audio using standard MediaPlayer
- App developer chooses storage path
- No storage permissions required by library

---

## Installation (JitPack)

### 1️⃣ Add JitPack to your **root `settings.gradle` or `build.gradle`**

```gradle
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}
```
### Add Dependency
```
dependencies {
	        implementation 'com.github.Excelsior-Technologies-Community:Android_VoiceRecorder:1.0.1'
	}
```

---

### Basic Usage

Initialize VoiceRecorder
```kotlin
val recorder = VoiceRecorder(object : VoiceRecorderCallback {

    override fun onStart() {
        Log.d("Recorder", "Recording started")
    }

    override fun onDuration(durationMs: Long) {
        val seconds = durationMs / 1000
        Log.d("Recorder", "Duration: $seconds sec")
    }

    override fun onStop(filePath: String) {
        Log.d("Recorder", "Recording saved at: $filePath")
    }

    override fun onError(error: String) {
        Log.e("Recorder", error)
    }
})
```

Start Recording
```
recorder.startRecording(
    path = filesDir.absolutePath,
    fileName = "voice_${System.currentTimeMillis()}.m4a"
)
```

Pause / Resume Recording (API 24+)
```
recorder.pauseRecording()
recorder.resumeRecording()
```

Stop Recording
```
recorder.stopRecording()
```

Playing the Recording (Example)
```
val mediaPlayer = MediaPlayer().apply {
    setDataSource(filePath)
    prepare()
    start()
}
```

### Example Usage

XML
```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:id="@+id/main"
    android:background="@color/white"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:gravity="center"
    android:orientation="vertical"
    android:padding="24dp">

    <!-- ⏱ Recording Timer -->
    <TextView
        android:id="@+id/tvTimer"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="00:00"
        android:textSize="24sp"
        android:textStyle="bold"
        android:textColor="@android:color/black"
        android:layout_marginBottom="24dp" />

    <Button
        android:id="@+id/btnStart"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Start Recording" />

    <Button
        android:id="@+id/btnPause"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Pause"
        android:layout_marginTop="12dp" />

    <Button
        android:id="@+id/btnResume"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Resume"
        android:layout_marginTop="12dp" />

    <Button
        android:id="@+id/btnStop"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Stop Recording"
        android:layout_marginTop="12dp" />

    <Button
        android:id="@+id/btnPlay"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Play Recording"
        android:layout_marginTop="12dp" />

</LinearLayout>
```

Kotlin
```kotlin

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
```

---

### License

```
MIT License

Copyright (c) 2025 Excelsior Technologies 

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

