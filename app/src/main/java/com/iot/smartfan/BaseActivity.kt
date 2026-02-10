package com.iot.smartfan

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button;
import androidx.core.content.ContextCompat
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog

open class BaseActivity : AppCompatActivity() {

    var dbURL = "https://smartfan-26794-default-rtdb.asia-southeast1.firebasedatabase.app"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    fun setNavbarCurrentButton(currentButtonId: Int) {
        val btnHome = findViewById<Button>(R.id.button_home)
        val btnSettings = findViewById<Button>(R.id.button_settings)

        val defaultColor = ContextCompat.getColor(this, R.color.dark)
        val selectedColor = ContextCompat.getColor(this, R.color.highlight_color)

        btnHome.setTextColor(defaultColor)
        btnSettings.setTextColor(defaultColor)

        when (currentButtonId) {
            R.id.button_home -> btnHome.setTextColor(selectedColor)
            R.id.button_settings -> btnSettings.setTextColor(selectedColor)
        }
    }

    fun isInternetAvailable(): Boolean {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    fun showNoInternetDialog() {
        AlertDialog.Builder(this)
            .setTitle("Network error")
            .setMessage("This application will not run properly without internet connection.")
            .setCancelable(false)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    override fun onResume() {
        super.onResume()
        if (!isInternetAvailable()) {
            showNoInternetDialog()
        }
    }

    fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    fun updateSwitchImage(button: ImageButton, value: Int) {
        if (value == 1) {
            button.setImageResource(R.drawable.on)
        }
        else {
            button.setImageResource(R.drawable.off)
        }
    }
}