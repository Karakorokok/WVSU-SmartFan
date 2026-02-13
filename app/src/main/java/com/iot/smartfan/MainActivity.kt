package com.iot.smartfan

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.content.Intent
import android.widget.Button
import android.widget.ImageButton
import com.google.firebase.database.*
import android.widget.TextView

class MainActivity : BaseActivity() {

    private val db = FirebaseDatabase.getInstance(dbURL)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        if (!isInternetAvailable()) {
            showNoInternetDialog()
        }

        setNavbarCurrentButton(R.id.button_home)

        val btnSettings = findViewById<Button>(R.id.button_settings)
        val btnSpeedOff = findViewById<ImageButton>(R.id.btn_speed_off)
        val btnSpeedLow = findViewById<ImageButton>(R.id.btn_speed_low)
        val btnSpeedMid = findViewById<ImageButton>(R.id.btn_speed_mid)
        val btnSpeedHigh = findViewById<ImageButton>(R.id.btn_speed_high)
        val btnSwingX = findViewById<ImageButton>(R.id.btn_swing_left_right)
        val btnSwingY = findViewById<ImageButton>(R.id.btn_swing_up_down)

        val modeValue = findViewById<TextView>(R.id.mode_value)
        val temperatureValue = findViewById<TextView>(R.id.temperature_value)
        val speedValue = findViewById<TextView>(R.id.speed_value)

        val dataRef = db.getReference("data")

        dataRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val mode = snapshot.child("mode").getValue(Int::class.java) ?: 0
                modeValue.text = if (mode == 0) "Manual" else "Automatic"

                val temp = snapshot.child("temperature").getValue(Double::class.java) ?: 0.0
                temperatureValue.text = String.format("%.2f°C", temp)

                val speed = snapshot.child("speed_status").getValue(Int::class.java) ?: 0
                updateSpeedButtons(speed)
                speedValue.text = when(speed) {
                    0 -> "Off"
                    1 -> "Low"
                    2 -> "Mid"
                    3 -> "High"
                    else -> "--"
                }

                val currentSwingX = snapshot.child("swing_x").getValue(Int::class.java) ?: 0
                updateSwitchImage(btnSwingX, currentSwingX)
                val currentSwingY = snapshot.child("swing_y").getValue(Int::class.java) ?: 0
                updateSwitchImage(btnSwingY, currentSwingY)
            }

            override fun onCancelled(error: DatabaseError) {
                println("Failed to read data: ${error.message}")
            }
        })

        btnSpeedOff.setOnClickListener { setFanSpeed(0) }
        btnSpeedLow.setOnClickListener { setFanSpeed(1) }
        btnSpeedMid.setOnClickListener { setFanSpeed(2) }
        btnSpeedHigh.setOnClickListener { setFanSpeed(3) }

        btnSwingX.setOnClickListener { toggleSwing("swing_x", btnSwingX) }
        btnSwingY.setOnClickListener { toggleSwing("swing_y", btnSwingY) }

        btnSettings.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }
    }

    private fun setFanSpeed(speed: Int) {
        db.getReference("data").child("speed_command").setValue(speed)
            .addOnFailureListener {
                println("Failed to update fan speed: ${it.message}")
            }
    }

    private fun toggleSwing(path: String, button: ImageButton) {
        val swingRef = db.getReference("data/$path")
        swingRef.get().addOnSuccessListener { snapshot ->
            val current = snapshot.getValue(Int::class.java) ?: 0
            val newValue = if (current == 1) 0 else 1
            swingRef.setValue(newValue)
                .addOnFailureListener {
                    println("Failed to update $path: ${it.message}")
                }
        }
    }

    private fun updateSpeedButtons(speed: Int) {
        val btnSpeedOff = findViewById<ImageButton>(R.id.btn_speed_off)
        val btnSpeedLow = findViewById<ImageButton>(R.id.btn_speed_low)
        val btnSpeedMid = findViewById<ImageButton>(R.id.btn_speed_mid)
        val btnSpeedHigh = findViewById<ImageButton>(R.id.btn_speed_high)

        btnSpeedOff.setImageResource(if (speed == 0) R.drawable.on else R.drawable.off)
        btnSpeedLow.setImageResource(if (speed == 1) R.drawable.on else R.drawable.off)
        btnSpeedMid.setImageResource(if (speed == 2) R.drawable.on else R.drawable.off)
        btnSpeedHigh.setImageResource(if (speed == 3) R.drawable.on else R.drawable.off)
    }

}
