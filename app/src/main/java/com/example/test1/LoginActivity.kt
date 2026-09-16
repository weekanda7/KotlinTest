package com.example.test1

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.test1.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.buttonLogin.setOnClickListener { attemptLogin() }

        binding.buttonSkip.visibility = if (BuildConfig.DEBUG) View.VISIBLE else View.GONE
        binding.buttonSkip.setOnClickListener { skipLogin() }
    }

    private fun skipLogin() {
        val intent = Intent(this, HomeActivity::class.java).apply {
            putExtra(HomeActivity.EXTRA_USERNAME, "debug@example.com")
            putExtra(HomeActivity.EXTRA_REMEMBER_ME, false)
        }
        startActivity(intent)
        finish()
    }

    private fun attemptLogin() {
        binding.inputLayoutUsername.error = null
        binding.inputLayoutPassword.error = null

        val username = binding.editUsername.text?.toString().orEmpty()
        val password = binding.editPassword.text?.toString().orEmpty()
        var hasError = false

        if (username.isBlank()) {
            binding.inputLayoutUsername.error = getString(R.string.error_username_required)
            hasError = true
        } else if (!Patterns.EMAIL_ADDRESS.matcher(username).matches()) {
            binding.inputLayoutUsername.error = getString(R.string.error_username_invalid)
            hasError = true
        }

        if (password.isBlank()) {
            binding.inputLayoutPassword.error = getString(R.string.error_password_required)
            hasError = true
        } else if (password.length < 6) {
            binding.inputLayoutPassword.error = getString(R.string.error_password_too_short)
            hasError = true
        }

        if (hasError) return

        val intent = Intent(this, HomeActivity::class.java).apply {
            putExtra(HomeActivity.EXTRA_USERNAME, username)
            putExtra(HomeActivity.EXTRA_REMEMBER_ME, binding.checkboxRememberMe.isChecked)
        }
        startActivity(intent)
        finish()
    }
}
