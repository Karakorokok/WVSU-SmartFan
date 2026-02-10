package com.iot.smartfan

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import android.widget.ImageButton
import androidx.cardview.widget.CardView

class SettingsActivity : BaseActivity() {

    private val db = FirebaseDatabase.getInstance(dbURL)
    private lateinit var editTextMap: Map<EditText, String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_settings)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        if (!isInternetAvailable()) {
            showNoInternetDialog()
        }

        setNavbarCurrentButton(R.id.button_settings)

        val btnHome = findViewById<Button>(R.id.button_home)
        val btnManual = findViewById<ImageButton>(R.id.btn_manual)
        val btnAutomatic = findViewById<ImageButton>(R.id.btn_automatic)
        val btnSavePreset = findViewById<Button>(R.id.btn_save_preset)

        val et_off = findViewById<EditText>(R.id.et_auto_off)
        val et_low = findViewById<EditText>(R.id.et_auto_low)
        val et_mid = findViewById<EditText>(R.id.et_auto_mid)
        val et_high = findViewById<EditText>(R.id.et_auto_high)

        val cardPreset = findViewById<CardView>(R.id.card_preset)

        val dataRef = db.getReference("data")
        dataRef.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {

                val currentMode = snapshot.child("mode").getValue(Int::class.java) ?: 0

                if (currentMode == 0) {
                    updateSwitchImage(btnManual, 1)
                    updateSwitchImage(btnAutomatic, 0)
                    cardPreset.visibility = View.GONE
                }
                else {
                    updateSwitchImage(btnManual, 0)
                    updateSwitchImage(btnAutomatic, 1)
                    cardPreset.visibility = View.VISIBLE
                }

                val off = snapshot.child("value_off").getValue(Int::class.java)
                val low = snapshot.child("value_low").getValue(Int::class.java)
                val mid = snapshot.child("value_mid").getValue(Int::class.java)
                val high = snapshot.child("value_high").getValue(Int::class.java)

                off?.let { et_off.setText(it.toString()) }
                low?.let { et_low.setText(it.toString()) }
                mid?.let { et_mid.setText(it.toString()) }
                high?.let { et_high.setText(it.toString()) }

            }

            override fun onCancelled(error: DatabaseError) {
                println("Failed to read data: ${error.message}")
            }
        })

        btnManual.setOnClickListener {
            db.getReference("data/mode").setValue(0)
                .addOnFailureListener {
                    println("Failed to set Manual mode: ${it.message}")
                }
        }

        btnAutomatic.setOnClickListener {
            db.getReference("data/mode").setValue(1)
                .addOnFailureListener {
                    println("Failed to set Automatic mode: ${it.message}")
                }
        }

        btnSavePreset.setOnClickListener {

            val off = et_off.text.toString().trim()
            val low = et_low.text.toString().trim()
            val mid = et_mid.text.toString().trim()
            val high = et_high.text.toString().trim()

            if (off.isEmpty() || low.isEmpty() || mid.isEmpty() || high.isEmpty()) {
                return@setOnClickListener
            }

            val updates = mapOf(
                "value_off" to off.toInt(),
                "value_low" to low.toInt(),
                "value_mid" to mid.toInt(),
                "value_high" to high.toInt()
            )

            db.getReference("data").updateChildren(updates)
                .addOnSuccessListener {
                    showToast("Preset saved")
                }
                .addOnFailureListener {
                    println("Failed to save preset: ${it.message}")
                }
        }

        btnHome.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }
    }
}