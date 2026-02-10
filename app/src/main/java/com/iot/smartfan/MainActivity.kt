package com.iot.smartfan

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.content.Intent
import android.widget.Button;
import android.widget.ImageButton
import com.google.firebase.database.*
import com.google.firebase.database.FirebaseDatabase
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

                val currentOffValue = snapshot.child("off").getValue(Int::class.java) ?: 0
                updateSwitchImage(btnSpeedOff, currentOffValue)
                val currentLowValue = snapshot.child("low").getValue(Int::class.java) ?: 0
                updateSwitchImage(btnSpeedLow, currentLowValue)
                val currentMidValue = snapshot.child("mid").getValue(Int::class.java) ?: 0
                updateSwitchImage(btnSpeedMid, currentMidValue)
                val currentHighValue = snapshot.child("high").getValue(Int::class.java) ?: 0
                updateSwitchImage(btnSpeedHigh, currentHighValue)

                val currentSwingX = snapshot.child("swing_x").getValue(Int::class.java) ?: 0
                updateSwitchImage(btnSwingX, currentSwingX)
                val currentSwingY = snapshot.child("swing_y").getValue(Int::class.java) ?: 0
                updateSwitchImage(btnSwingY, currentSwingY)

                val off = snapshot.child("off").getValue(Int::class.java) ?: 0
                val low = snapshot.child("low").getValue(Int::class.java) ?: 0
                val mid = snapshot.child("mid").getValue(Int::class.java) ?: 0
                val high = snapshot.child("high").getValue(Int::class.java) ?: 0

                val speedText = when {
                    off  == 1 -> "Off"
                    high == 1 -> "High"
                    mid  == 1 -> "Mid"
                    low  == 1 -> "Low"
                    else -> "--"
                }

                speedValue.text = speedText
            }

            override fun onCancelled(error: DatabaseError) {
                println("Failed to read data: ${error.message}")
            }
        })

        btnSpeedOff.setOnClickListener {
            setFanState(
                off = 1,
                low = 0,
                mid = 0,
                high = 0
            )
        }

        btnSpeedLow.setOnClickListener {
            setFanState(
                off = 0,
                low = 1,
                mid = 0,
                high = 0
            )
        }

        btnSpeedMid.setOnClickListener {
            setFanState(
                off = 0,
                low = 0,
                mid = 1,
                high = 0
            )
        }

        btnSpeedHigh.setOnClickListener {
            setFanState(
                off = 0,
                low = 0,
                mid = 0,
                high = 1
            )
        }

        btnSwingX.setOnClickListener {
            toggleSwing("swing_x", btnSwingX)
        }

        btnSwingY.setOnClickListener {
            toggleSwing("swing_y", btnSwingY)
        }

        btnSettings.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }
    }

    private fun setFanState(off: Int, low: Int, mid: Int, high: Int) {
        val updates = mapOf(
            "off" to off,
            "low" to low,
            "mid" to mid,
            "high" to high
        )

        db.getReference("data").updateChildren(updates)
            .addOnFailureListener {
                println("Failed to update fan state: ${it.message}")
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

}