package com.example.clockfacewidget

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.clockfacewidget.widget.ClockWidgetProvider
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    private lateinit var preview: ImageView
    private lateinit var spinner: Spinner
    private lateinit var secondsSwitch: Switch
    private var selectedFace = ClockFace.BLACK

    // TAMBAHAN: Variabel untuk jam digital
    private lateinit var digitalSpinner: Spinner
    private var selectedDigitalStyle = DigitalStyle.NONE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        preview = findViewById(R.id.preview)
        spinner = findViewById(R.id.faceSpinner)
        secondsSwitch = findViewById(R.id.secondsSwitch)
        
        // TAMBAHAN: Inisialisasi spinner jam digital
        digitalSpinner = findViewById(R.id.digitalSpinner)

        // Setup Spinner Tema Jam
        val labels = ClockFace.values().map { it.label }
        spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, labels)

        // TAMBAHAN: Setup Spinner Gaya Jam Digital
        val digitalLabels = DigitalStyle.values().map { it.label }
        digitalSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, digitalLabels)

        // Membaca pengaturan yang tersimpan
        selectedFace = ClockSettings.face(this)
        secondsSwitch.isChecked = ClockSettings.seconds(this)
        selectedDigitalStyle = ClockSettings.digitalStyle(this) // TAMBAHAN
        
        spinner.setSelection(selectedFace.ordinal)
        digitalSpinner.setSelection(selectedDigitalStyle.ordinal) // TAMBAHAN

        // Listener Spinner Tema
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                selectedFace = ClockFace.values()[position]
                refreshPreview()
            }
        }

        // TAMBAHAN: Listener Spinner Jam Digital
        digitalSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                selectedDigitalStyle = DigitalStyle.values()[position]
                refreshPreview()
            }
        }

        secondsSwitch.setOnCheckedChangeListener { _, _ -> refreshPreview() }

        // Tombol Simpan
        findViewById<Button>(R.id.applyButton).setOnClickListener {
            ClockSettings.save(this, selectedFace, secondsSwitch.isChecked, selectedDigitalStyle) // TAMBAHAN
            ClockWidgetProvider.updateAll(this)
            Toast.makeText(this, "Clock Face disimpan.", Toast.LENGTH_SHORT).show()
        }

        refreshPreview()
    }

    private fun refreshPreview() {
        val bitmap = ClockRenderer.render(
            size = 700,
            face = selectedFace,
            showSeconds = secondsSwitch.isChecked,
            digitalStyle = selectedDigitalStyle, // TAMBAHAN
            calendar = Calendar.getInstance()
        )
        preview.setImageBitmap(bitmap)
    }

    override fun onResume() {
        super.onResume()
        refreshPreview()
    }
}
