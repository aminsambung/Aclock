package com.example.clockfacewidget

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val button = findViewById<Button>(R.id.applyButton)
        button.setOnClickListener {
            Toast.makeText(this, "Aplikasi Berjalan Normal!", Toast.LENGTH_SHORT).show()
        }
    }
}
