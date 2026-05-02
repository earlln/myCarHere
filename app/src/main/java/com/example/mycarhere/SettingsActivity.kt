package com.example.mycarhere

import android.content.Intent
import android.os.Bundle
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mycarhere.Prefs.allowMultiLocation
import com.example.mycarhere.Prefs.allowMultiPhoto
import com.example.mycarhere.Prefs.allowMultiVoice
import com.example.mycarhere.Prefs.recordSeconds
import com.example.mycarhere.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.title = "설정"
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Load current settings
        val secs = recordSeconds
        // SeekBar: 0..25 → 1..30 offset +5 wait: progress = secs - 5, clamped to 0..25
        binding.seekBarRecordTime.progress = (secs - 5).coerceIn(0, 25)
        binding.tvRecordTimeValue.text = "${secs}초"

        binding.switchMultiLocation.isChecked = allowMultiLocation
        binding.switchMultiPhoto.isChecked = allowMultiPhoto
        binding.switchMultiVoice.isChecked = allowMultiVoice

        binding.seekBarRecordTime.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar, progress: Int, fromUser: Boolean) {
                val v = progress + 5   // 5..30
                binding.tvRecordTimeValue.text = "${v}초"
            }
            override fun onStartTrackingTouch(sb: SeekBar) {}
            override fun onStopTrackingTouch(sb: SeekBar) {}
        })

        binding.btnManagePlaces.setOnClickListener {
            startActivity(Intent(this, PlaceListActivity::class.java))
        }

        binding.btnSaveSettings.setOnClickListener {
            val secs = binding.seekBarRecordTime.progress + 5
            recordSeconds = secs
            allowMultiLocation = binding.switchMultiLocation.isChecked
            allowMultiPhoto = binding.switchMultiPhoto.isChecked
            allowMultiVoice = binding.switchMultiVoice.isChecked
            Toast.makeText(this, "설정이 저장되었습니다", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
