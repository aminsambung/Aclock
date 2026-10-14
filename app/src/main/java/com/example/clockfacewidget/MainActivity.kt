package com.example.clockfacewidget

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

    private var selectedType = ClockType.ANALOG
    private var selectedAnalogTheme = AnalogTheme.BLACK
    private var selectedDigitalTheme = DigitalTheme.BIG_WHITE

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

        // Setup Spinner Tipe Jam
        typeSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, ClockType.values().map { it.label })
        analogSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, AnalogTheme.values().map { it.label })
        digitalSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, DigitalTheme.values().map { it.label })

        // Baca pengaturan tersimpan
        selectedType = ClockSettings.type(this)
        selectedAnalogTheme = ClockSettings.analogTheme(this)
        selectedDigitalTheme = ClockSettings.digitalTheme(this)
        secondsSwitch.isChecked = ClockSettings.seconds(this)

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

        findViewById<Button>(R.id.applyButton).setOnClickListener {
            ClockSettings.save(this, selectedType, selectedAnalogTheme, selectedDigitalTheme, secondsSwitch.isChecked)
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

    private fun refreshPreview() {
        val bitmap = ClockRenderer.render(
            size = 700,
            type = selectedType,
            analogTheme = selectedAnalogTheme,
            digitalTheme = selectedDigitalTheme,
            showSeconds = secondsSwitch.isChecked,
            calendar = Calendar.getInstance()
        )
        preview.setImageBitmap(bitmap)
    }

    override fun onResume() {
        super.onResume()
        refreshPreview()
    }
}
