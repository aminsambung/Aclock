package com.example.clockfacewidget

import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.clockfacewidget.widget.ClockWidgetProvider
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        try {
            // 1. Inisialisasi View
            val preview = findViewById<ImageView>(R.id.preview)
            val typeSpinner = findViewById<Spinner>(R.id.typeSpinner)
            val analogSpinner = findViewById<Spinner>(R.id.analogSpinner)
            val digitalSpinner = findViewById<Spinner>(R.id.digitalSpinner)
            val analogLabel = findViewById<TextView>(R.id.analogLabel)
            val digitalLabel = findViewById<TextView>(R.id.digitalLabel)
            val secondsSwitch = findViewById<Switch>(R.id.secondsSwitch)
            val secondsLabel = findViewById<TextView>(R.id.secondsLabel)
            val alarmSwitch = findViewById<Switch>(R.id.alarmSwitch)
            val alarmTimeButton = findViewById<Button>(R.id.alarmTimeButton)
            val applyButton = findViewById<Button>(R.id.applyButton)

            // 2. Variabel status
            var selectedType = ClockSettings.type(this)
            var selectedAnalogTheme = ClockSettings.analogTheme(this)
            var selectedDigitalTheme = ClockSettings.digitalTheme(this)
            var alarmEnabled = ClockSettings.isAlarmEnabled(this)
            var alarmHour = ClockSettings.alarmHour(this)
            var alarmMinute = ClockSettings.alarmMinute(this)

            // 3. Setup Spinner Adapter
            typeSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, ClockType.values().map { it.label })
            analogSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, AnalogTheme.values().map { it.label })
            digitalSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, DigitalTheme.values().map { it.label })

            // 4. Set nilai awal
            typeSpinner.setSelection(selectedType.ordinal)
            analogSpinner.setSelection(selectedAnalogTheme.ordinal)
            digitalSpinner.setSelection(selectedDigitalTheme.ordinal)
            secondsSwitch.isChecked = ClockSettings.seconds(this)
            alarmSwitch.isChecked = alarmEnabled

            // 5. Fungsi update pratinjau dengan pengaman
            fun refreshPreview() {
                try {
                    val bitmap = ClockRenderer.render(
                        size = 700,
                        type = selectedType,
                        analogTheme = selectedAnalogTheme,
                        digitalTheme = selectedDigitalTheme,
                        showSeconds = secondsSwitch.isChecked,
                        alarmEnabled = alarmEnabled,
                        alarmHour = alarmHour,
                        alarmMinute = alarmMinute,
                        calendar = Calendar.getInstance()
                    )
                    preview.setImageBitmap(bitmap)
                } catch (e: Exception) {
                    Log.e("ClockApp", "Error render preview: ${e.message}")
                }
            }

            // 6. Fungsi update visibilitas
            fun updateUiVisibility() {
                val isAnalog = selectedType == ClockType.ANALOG
                analogLabel.visibility = if (isAnalog) View.VISIBLE else View.GONE
                analogSpinner.visibility = if (isAnalog) View.VISIBLE else View.GONE
                secondsLabel.visibility = if (isAnalog) View.VISIBLE else View.GONE
                secondsSwitch.visibility = if (isAnalog) View.VISIBLE else View.GONE
                digitalLabel.visibility = if (!isAnalog) View.VISIBLE else View.GONE
                digitalSpinner.visibility = if (!isAnalog) View.VISIBLE else View.GONE
            }

            // 7. Fungsi update tombol alarm
            fun updateAlarmUi() {
                val amPm = if (alarmHour < 12) "AM" else "PM"
                val displayHour = if (alarmHour == 0) 12 else if (alarmHour > 12) alarmHour - 12 else alarmHour
                alarmTimeButton.text = String.format("%02d:%02d %s", displayHour, alarmMinute, amPm)
                alarmTimeButton.isEnabled = alarmEnabled
            }

            // 8. Listener Spinner Tipe
            typeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) = Unit
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    selectedType = ClockType.values()[position]
                    updateUiVisibility()
                    refreshPreview()
                }
            }

            // 9. Listener Spinner Tema Analog
            analogSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) = Unit
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    selectedAnalogTheme = AnalogTheme.values()[position]
                    refreshPreview()
                }
            }

            // 10. Listener Spinner Tema Digital
            digitalSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) = Unit
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    selectedDigitalTheme = DigitalTheme.values()[position]
                    refreshPreview()
                }
            }

            // 11. Listener Sakelar Detik
            secondsSwitch.setOnCheckedChangeListener { _, _ -> refreshPreview() }

            // 12. Listener Sakelar Alarm
            alarmSwitch.setOnCheckedChangeListener { _, isChecked ->
                alarmEnabled = isChecked
                updateAlarmUi()
                refreshPreview()
            }

            // 13. Listener Tombol Waktu Alarm
            alarmTimeButton.setOnClickListener {
                TimePickerDialog(this, { _, h, m ->
                    alarmHour = h
                    alarmMinute = m
                    updateAlarmUi()
                    refreshPreview()
                }, alarmHour, alarmMinute, true).show()
            }

            // 14. Listener Tombol Simpan
            applyButton.setOnClickListener {
                ClockSettings.save(
                    this, selectedType, selectedAnalogTheme, selectedDigitalTheme,
                    secondsSwitch.isChecked, alarmEnabled, alarmHour, alarmMinute
                )
                ClockWidgetProvider.updateAll(this)
                Toast.makeText(this, "Pengaturan disimpan.", Toast.LENGTH_SHORT).show()
            }

            // 15. Inisialisasi awal
            updateUiVisibility()
            updateAlarmUi()
            refreshPreview()

        } catch (e: Exception) {
            // Jika ada error, jangan crash, tapi tampilkan pesan
            Log.e("ClockApp", "FATAL ERROR: ${e.message}", e)
            Toast.makeText(this, "Terjadi kesalahan: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
