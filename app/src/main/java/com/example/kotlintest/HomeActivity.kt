package com.example.kotlintest

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.kotlintest.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.home.applySystemBarInsetsPadding()

        val username = intent.getStringExtra(EXTRA_USERNAME).orEmpty()
        val rememberMe = intent.getBooleanExtra(EXTRA_REMEMBER_ME, false)

        binding.textWelcome.text = getString(R.string.text_welcome, username)
        binding.textRemembered.visibility = if (rememberMe) View.VISIBLE else View.GONE

        binding.buttonViewDevices.setOnClickListener {
            startActivity(Intent(this, DeviceListActivity::class.java))
        }
        binding.buttonLogout.setOnClickListener { finish() }
    }

    companion object {
        const val EXTRA_USERNAME = "extra_username"
        const val EXTRA_REMEMBER_ME = "extra_remember_me"
    }
}
