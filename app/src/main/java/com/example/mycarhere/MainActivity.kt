package com.example.mycarhere

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Environment
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mycarhere.Prefs.allowMultiLocation
import com.example.mycarhere.Prefs.allowMultiPhoto
import com.example.mycarhere.Prefs.allowMultiVoice
import com.example.mycarhere.Prefs.recordSeconds
import com.example.mycarhere.Prefs.autoRecordOnLaunch
import com.example.mycarhere.Prefs.autoPlayOnLaunch
import com.example.mycarhere.databinding.ActivityMainBinding
import kotlinx.coroutines.launch
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var db: AppDatabase

    private val selectedLocations = mutableSetOf<String>()
    private val photoPaths = mutableListOf<String>()
    private val audioPaths = mutableListOf<String>()

    private var mediaRecorder: MediaRecorder? = null
    private var countDownTimer: CountDownTimer? = null
    private var currentAudioPath: String? = null

    private var mediaPlayer: MediaPlayer? = null

    private var pendingPhotoPath: String? = null

    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && pendingPhotoPath != null) {
            val path = pendingPhotoPath!!
            if (!allowMultiPhoto) photoPaths.clear()
            photoPaths.add(path)
            saveRecord()
            updateStatusUI()
        }
    }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        val audioGranted = grants[Manifest.permission.RECORD_AUDIO] ?: false
        val cameraGranted = grants[Manifest.permission.CAMERA] ?: false
        if (audioGranted) startAutoRecord()
        if (cameraGranted) launchCamera()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDatabase.get(this)

        val versionName = packageManager.getPackageInfo(packageName, 0).versionName
        binding.tvVersion.text = "v$versionName"

        setupClickListeners()
        loadLastRecord()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopRecording(save = false)
        mediaPlayer?.release()
    }

    private fun setupClickListeners() {
        binding.btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        binding.btnSelectLocation.setOnClickListener { showLocationDialog() }
        binding.btnTakePhoto.setOnClickListener { requestCameraAndShoot() }
        binding.btnRecordVoice.setOnClickListener { requestAudioAndRecord() }
        binding.btnStopRecording.setOnClickListener { stopRecording(save = true) }
        binding.btnPlayVoice.setOnClickListener { playLatestAudio() }
        binding.btnClearAll.setOnClickListener { confirmClearAll() }
    }

    private fun loadLastRecord() {
        lifecycleScope.launch {
            val rec = db.carRecordDao().get()
            if (rec != null) {
                selectedLocations.clear()
                photoPaths.clear()
                audioPaths.clear()

                rec.selectedLocationNames.split(",")
                    .filter { it.isNotBlank() }
                    .forEach { selectedLocations.add(it) }

                rec.photoPaths.split(",")
                    .filter { it.isNotBlank() && File(it).exists() }
                    .forEach { photoPaths.add(it) }

                rec.audioPaths.split(",")
                    .filter { it.isNotBlank() && File(it).exists() }
                    .forEach { audioPaths.add(it) }

                updateStatusUI()

                // 설정에 따라 자동 재생
                if (autoPlayOnLaunch && audioPaths.isNotEmpty()) {
                    binding.root.postDelayed({ playLatestAudio() }, 400)
                }
                // 설정에 따라 자동 녹음
                if (autoRecordOnLaunch) {
                    scheduleAutoRecord()
                }
            } else {
                if (autoRecordOnLaunch) scheduleAutoRecord()
            }
        }
    }

    private fun scheduleAutoRecord() {
        binding.root.postDelayed({ requestAudioAndRecord(isAuto = true) }, 600)
    }

    private fun updateStatusUI() {
        if (selectedLocations.isEmpty() && photoPaths.isEmpty() && audioPaths.isEmpty()) {
            binding.tvLastLocation.text = "저장된 정보가 없습니다"
            binding.layoutStatusIcons.visibility = View.GONE
            binding.scrollPhotos.visibility = View.GONE
            binding.btnPlayVoice.visibility = View.GONE
            return
        }

        binding.tvLastLocation.text = if (selectedLocations.isEmpty())
            "(장소 미선택)" else selectedLocations.joinToString(", ")

        binding.layoutStatusIcons.visibility = View.VISIBLE
        binding.tvHasPhoto.visibility = if (photoPaths.isNotEmpty()) View.VISIBLE else View.GONE
        binding.tvHasVoice.visibility = if (audioPaths.isNotEmpty()) View.VISIBLE else View.GONE

        // 여러 사진 썸네일 — 가로 스크롤
        binding.layoutPhotos.removeAllViews()
        if (photoPaths.isNotEmpty()) {
            val density = resources.displayMetrics.density
            val thumbWidth = (140 * density).toInt()
            val thumbHeight = binding.scrollPhotos.layoutParams.height
            val margin = (6 * density).toInt()

            for (path in photoPaths) {
                val bmp = BitmapFactory.decodeFile(path) ?: continue
                val iv = ImageView(this).apply {
                    layoutParams = android.widget.LinearLayout.LayoutParams(thumbWidth, thumbHeight).also {
                        it.marginEnd = margin
                    }
                    scaleType = ImageView.ScaleType.CENTER_CROP
                    setImageBitmap(bmp)
                }
                binding.layoutPhotos.addView(iv)
            }
            binding.scrollPhotos.visibility = View.VISIBLE
        } else {
            binding.scrollPhotos.visibility = View.GONE
        }

        binding.btnPlayVoice.visibility = if (audioPaths.isNotEmpty()) View.VISIBLE else View.GONE
    }

    private fun showLocationDialog() {
        lifecycleScope.launch {
            val places = db.placeDao().getAll()
            if (places.isEmpty()) {
                AlertDialog.Builder(this@MainActivity)
                    .setTitle("장소 없음")
                    .setMessage("등록된 장소가 없습니다. 설정에서 장소를 추가해주세요.")
                    .setPositiveButton("설정으로") { _, _ ->
                        startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
                    }
                    .setNegativeButton("취소", null)
                    .show()
                return@launch
            }

            val tempSelected = selectedLocations.toMutableSet()
            val dialogView = layoutInflater.inflate(R.layout.dialog_select_location, null)
            val recycler = dialogView.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.recyclerLocations)
            recycler.layoutManager = LinearLayoutManager(this@MainActivity)
            recycler.adapter = LocationChoiceAdapter(
                items = places,
                selectedNames = tempSelected,
                multiSelect = allowMultiLocation,
                onPick = {}
            )

            AlertDialog.Builder(this@MainActivity)
                .setTitle("📍 장소 선택")
                .setView(dialogView)
                .setPositiveButton("확인") { _, _ ->
                    selectedLocations.clear()
                    selectedLocations.addAll(tempSelected)
                    saveRecord()
                    updateStatusUI()
                }
                .setNegativeButton("취소", null)
                .show()
        }
    }

    private fun requestCameraAndShoot() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED -> launchCamera()
            else -> permissionLauncher.launch(arrayOf(Manifest.permission.CAMERA))
        }
    }

    private fun launchCamera() {
        val dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES) ?: filesDir
        val file = File(dir, "photo_${System.currentTimeMillis()}.jpg")
        pendingPhotoPath = file.absolutePath
        val uri: Uri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", file)
        cameraLauncher.launch(uri)
    }

    private fun requestAudioAndRecord(isAuto: Boolean = false) {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                    == PackageManager.PERMISSION_GRANTED -> {
                if (isAuto) startAutoRecord() else startManualRecord()
            }
            else -> permissionLauncher.launch(arrayOf(Manifest.permission.RECORD_AUDIO))
        }
    }

    private fun startAutoRecord() { startRecording(seconds = recordSeconds) }
    private fun startManualRecord() { startRecording(seconds = recordSeconds) }

    private fun startRecording(seconds: Int) {
        if (mediaRecorder != null) return

        val dir = filesDir.resolve("audio").also { it.mkdirs() }
        val file = File(dir, "audio_${System.currentTimeMillis()}.3gp")
        currentAudioPath = file.absolutePath

        try {
            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setOutputFile(file.absolutePath)
                prepare()
                start()
            }
        } catch (e: Exception) {
            mediaRecorder = null
            Toast.makeText(this, "녹음 시작 실패", Toast.LENGTH_SHORT).show()
            return
        }

        binding.cardRecordingStatus.visibility = View.VISIBLE
        binding.tvRecordingStatus.text = "🎤 녹음 중... ${seconds}초"

        countDownTimer = object : CountDownTimer(seconds * 1000L, 1000) {
            override fun onTick(remaining: Long) {
                val s = (remaining / 1000).toInt() + 1
                binding.tvRecordingStatus.text = "🎤 녹음 중... ${s}초"
            }
            override fun onFinish() { stopRecording(save = true) }
        }.start()
    }

    private fun stopRecording(save: Boolean) {
        countDownTimer?.cancel()
        countDownTimer = null

        try { mediaRecorder?.apply { stop(); release() } } catch (_: Exception) {}
        mediaRecorder = null

        binding.cardRecordingStatus.visibility = View.GONE

        if (save && currentAudioPath != null && File(currentAudioPath!!).exists()) {
            if (!allowMultiVoice) audioPaths.clear()
            audioPaths.add(currentAudioPath!!)
            saveRecord()
            updateStatusUI()
        }
        currentAudioPath = null
    }

    private fun playLatestAudio() {
        val path = audioPaths.lastOrNull() ?: return
        mediaPlayer?.release()
        mediaPlayer = null

        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(path)
                setOnPreparedListener { mp ->
                    mp.start()
                    binding.btnPlayVoice.text = "⏸  재생 중..."
                }
                setOnCompletionListener { mp ->
                    binding.btnPlayVoice.text = "▶  음성 재생"
                    mp.release()
                    mediaPlayer = null
                }
                setOnErrorListener { mp, _, _ ->
                    binding.btnPlayVoice.text = "▶  음성 재생"
                    mp.release()
                    mediaPlayer = null
                    true
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "음성 재생 실패", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveRecord() {
        lifecycleScope.launch {
            db.carRecordDao().save(
                CarRecord(
                    selectedLocationNames = selectedLocations.joinToString(","),
                    photoPaths = photoPaths.joinToString(","),
                    audioPaths = audioPaths.joinToString(",")
                )
            )
        }
    }

    private fun confirmClearAll() {
        AlertDialog.Builder(this)
            .setTitle("전체 초기화")
            .setMessage("저장된 장소, 사진, 음성을 모두 지우시겠습니까?")
            .setPositiveButton("초기화") { _, _ ->
                selectedLocations.clear()
                photoPaths.clear()
                audioPaths.clear()
                saveRecord()
                updateStatusUI()
            }
            .setNegativeButton("취소", null)
            .show()
    }
}
