package com.example.mycarhere

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mycarhere.databinding.ActivityPlaceListBinding
import kotlinx.coroutines.launch

class PlaceListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlaceListBinding
    private lateinit var db: AppDatabase
    private lateinit var adapter: PlaceAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlaceListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDatabase.get(this)
        adapter = PlaceAdapter(mutableListOf(),
            onEdit = { place -> showEditDialog(place) },
            onDelete = { place -> confirmDelete(place) }
        )

        binding.recyclerPlaces.layoutManager = LinearLayoutManager(this)
        binding.recyclerPlaces.adapter = adapter

        binding.btnBack.setOnClickListener { finish() }
        binding.btnAddPlace.setOnClickListener { showAddDialog() }

        loadPlaces()
    }

    private fun loadPlaces() {
        lifecycleScope.launch {
            val places = db.placeDao().getAll()
            adapter.setData(places)
            binding.tvEmptyPlaces.visibility = if (places.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun showAddDialog() {
        val et = EditText(this).apply {
            hint = "장소 이름"
            setPadding(48, 32, 48, 32)
        }
        AlertDialog.Builder(this)
            .setTitle("장소 추가")
            .setView(et)
            .setPositiveButton("저장") { _, _ ->
                val name = et.text.toString().trim()
                if (name.isNotEmpty()) {
                    lifecycleScope.launch {
                        db.placeDao().insert(Place(name = name))
                        loadPlaces()
                    }
                } else {
                    Toast.makeText(this, "이름을 입력하세요", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun showEditDialog(place: Place) {
        val et = EditText(this).apply {
            hint = "장소 이름"
            setText(place.name)
            setPadding(48, 32, 48, 32)
        }
        AlertDialog.Builder(this)
            .setTitle("장소 수정")
            .setView(et)
            .setPositiveButton("저장") { _, _ ->
                val name = et.text.toString().trim()
                if (name.isNotEmpty()) {
                    lifecycleScope.launch {
                        db.placeDao().update(place.copy(name = name))
                        loadPlaces()
                    }
                }
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun confirmDelete(place: Place) {
        AlertDialog.Builder(this)
            .setTitle("장소 삭제")
            .setMessage("'${place.name}'을(를) 삭제하시겠습니까?")
            .setPositiveButton("삭제") { _, _ ->
                lifecycleScope.launch {
                    db.placeDao().delete(place)
                    loadPlaces()
                }
            }
            .setNegativeButton("취소", null)
            .show()
    }
}
