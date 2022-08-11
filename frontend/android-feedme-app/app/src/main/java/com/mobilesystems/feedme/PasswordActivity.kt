package com.mobilesystems.feedme

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.mobilesystems.feedme.ui.authentication.AuthViewModel
import com.mobilesystems.feedme.databinding.ActivityPasswordBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PasswordActivity : AppCompatActivity() {

    private val passwordForgottenViewModel: AuthViewModel by viewModels()
    private var _binding: ActivityPasswordBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogin.setOnClickListener {
            val intent = Intent(this@PasswordActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        val emailEditText = binding.email
        val resetButton = binding.reset
        val loadingProgressBar = binding.loading
        resetButton.isEnabled = false

        // To show back button in actionbar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        passwordForgottenViewModel.loginFormState.observe(this,
            Observer { passwordResetFormState ->
                if (passwordResetFormState == null) {
                    return@Observer
                }
                resetButton.isEnabled = passwordResetFormState.isDataValid
                passwordResetFormState.emailError?.let {
                    emailEditText.error = getString(it)
                }
            })

        resetButton.setOnClickListener {
            val intent = Intent(this@PasswordActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }


}