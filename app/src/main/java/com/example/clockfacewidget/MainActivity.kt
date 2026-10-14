package com.example.clockfacewidget

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.clockfacewidget.widget.ClockWidgetProvider
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    private lateinit var preview: ImageView
    private lateinit var typeSpinner: Spinner
    private lateinit var analogSpinner: Spinner
    private lateinit var digitalSpinner: Spinner
    private lateinit var analogLabel: TextView
    private lateinit var digitalLabel: TextView
    private lateinit var secondsSwitch: Switch
    private lateinit var secondsLabel: TextView
    
    // TAMBAHAN UNTUK ALARM
    private lateinit var alarmSwitch: Switch
    private lateinit var alarmTimeButton: Button

    private var selectedType = ClockType.ANALOG
    private var selectedAnalogTheme = AnalogTheme.BLACK
    private var selectedDigitalTheme = DigitalTheme.BIG_WHITE
    
    // TAMBAHAN UNTUK ALARM
    private var alarmEnabled = false
    private var alarmHour = 7
    private var alarmMinute = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        preview = findViewById(R.id.preview)
        typeSpinner = findViewById(R.id.typeSpinner)
        analogSpinner = findViewById(R.id.analogSpinner)
        digitalSpinner = findViewById(R.id.digitalSpinner)
        analogLabel = findViewById(R.id.analogLabel)
        digitalLabel = findViewById(R.id.digitalLabel)
        secondsSwitch = findViewById(R.id.secondsSwitch)
        secondsLabel = findViewById(R.id.secondsLabel)
        
        // TAMBAHAN UNTUK ALARM
        alarmSwitch = findViewById(R.id.alarmSwitch)
        alarmTimeButton = findViewById(R.id.alarmTimeButton)

        // Setup Spinner
        typeSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, ClockType.values().map { it.label })
        analogSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, AnalogTheme.values().map { it.label })
        digitalSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, DigitalTheme.values().map { it.label })

        // Baca pengaturan tersimpan
        selectedType = ClockSettings.type(this)
        selectedAnalogTheme = ClockSettings.analogTheme(this)
        selectedDigitalTheme = ClockSettings.digitalTheme(this)
        secondsSwitch.isChecked = ClockSettings.seconds(this)
        
        // TAMBAHAN UNTUK ALARM
        alarmEnabled = ClockSettings.isAlarmEnabled(this)
        alarmHour = ClockSettings.alarmHour(this)
        alarmMinute = ClockSettings.alarmMinute(this)
        alarmSwitch.isChecked = alarmEnabled

        typeSpinner.setSelection(selectedType.ordinal)
        analogSpinner.setSelection(selectedAnalogTheme.ordinal)
        digitalSpinner.setSelection(selectedDigitalTheme.ordinal)

        // Listener
        typeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedType = ClockType.values()[position]
                updateUiVisibility()
                refreshPreview()
            }
        }

        analogSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedAnalogTheme = AnalogTheme.values()[position]
                refreshPreview()
            }
        }

        digitalSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedDigitalTheme = DigitalTheme.values()[position]
                refreshPreview()
            }
        }

        secondsSwitch.setOnCheckedChangeListener { _, _ -> refreshPreview() }
        
        // TAMBAHAN UNTUK ALARM: Listener Sakelar
        alarmSwitch.setOnCheckedChangeListener { _, isChecked ->
            alarmEnabled = isChecked
            updateAlarmUi()
            refreshPreview()
        }
        
        // TAMBAHAN UNTUK ALARM: Tombol Pilih Waktu
        alarmTimeButton.setOnClickListener {
            val timePicker = TimePickerDialog(this, { _, h, m ->
                alarmHour = h
                alarmMinute = m
                updateAlarmUi()
                refreshPreview()
            }, alarmHour, alarmMinute, true)
            timePicker.show()
        }
        
        // Inisialisasi tampilan tombol alarm
        updateAlarmUi()

        findViewById<Button>(R.id.applyButton).setOnClickListener {
            ClockSettings.save(
                this, selectedType, selectedAnalogTheme, selectedDigitalTheme, 
                secondsSwitch.isChecked, alarmEnabled, alarmHour, alarmMinute
            )
            ClockWidgetProvider.updateAll(this)
            Toast.makeText(this, "Pengaturan disimpan.", Toast.LENGTH_SHORT).show()
        }

        updateUiVisibility()
        refreshPreview()
    }

    private fun updateUiVisibility() {
        val isAnalog = selectedType == ClockType.ANALOG
        analogLabel.visibility = if (isAnalog) View.VISIBLE else View.GONE
        analogSpinner.visibility = if (isAnalog) View.VISIBLE else View.GONE
        secondsLabel.visibility = if (isAnalog) View.VISIBLE else View.GONE
        secondsSwitch.visibility = if (isAnalog) View.VISIBLE else View.GONE

        digitalLabel.visibility = if (!isAnalog) View.VISIBLE else View.GONE
        digitalSpinner.visibility = if (!isAnalog) View.VISIBLE else View.GONE
    }
    
    // TAMBAHAN: Fungsi untuk memperbarui tampilan tombol alarm
    private fun updateAlarmUi() {
        val amPm = if (alarmHour < 12) "AM" else "PM"
        val displayHour = if (alarmHour == 0) 12 else if (alarmHour > 12) alarmHour - 12 else alarmHour
        alarmTimeButton.text = String.format("%02d:%02d %s", displayHour, alarmMinute, amPm)
        alarmTimeButton.isEnabled = alarmEnabled
    }

    private fun refreshPreview() {
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
    }

    override fun onResume() {
        super.onResume()
        refreshPreview()
    }
}
