package com.mobilesystems.feedme

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import com.mobilesystems.feedme.databinding.ActivityLoginBinding
import com.mobilesystems.feedme.ui.authentication.AuthViewModel
import androidx.lifecycle.Observer
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginActivity: AppCompatActivity() {

    // lazy delegate
    private val loginAuthViewModel: AuthViewModel by viewModels()

    private var _binding: ActivityLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSignUp.setOnClickListener {
            val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.forgotPassword.setOnClickListener {
            val intent = Intent(this@LoginActivity, PasswordActivity::class.java)
            startActivity(intent)
            finish()
        }

        val emailEditText = binding.email
        val passwordEditText = binding.password
        val loginButton = binding.login
        val loadingProgressBar = binding.loading
        loginButton.isEnabled = false

        loginAuthViewModel.loginFormState.observe(this,
            Observer { loginFormState ->
                if (loginFormState == null) {
                    return@Observer
                }
                loginButton.isEnabled = loginFormState.isDataValid
                loginFormState.emailError?.let {
                    emailEditText.error = getString(it)
                }
                loginFormState.passwordError?.let {
                    passwordEditText.error = getString(it)
                }
            })

        loginAuthViewModel.loginResult.observe(this,
            Observer { loginResult ->
                loginResult ?: return@Observer
                loadingProgressBar.visibility = View.GONE
                loginResult.error?.let {
                    showLoginFailed(it)
                }
                loginResult.success?.let {
                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            })

        val afterTextChangedListener = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                // check Fields For Empty Values
                loginButton.isEnabled =
                    !(emailEditText.equals("") || passwordEditText.equals(""))
            }

            override fun afterTextChanged(editable: Editable) {
                loginAuthViewModel.observeLoginDataChanged(
                    emailEditText.text.toString(),
                    passwordEditText.text.toString()
                )
            }
        }

        emailEditText.addTextChangedListener(afterTextChangedListener)
        passwordEditText.addTextChangedListener(afterTextChangedListener)
        passwordEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                loginAuthViewModel.login(
                    emailEditText.text.toString(),
                    passwordEditText.text.toString()
                )
            }
            false
        }

        loginButton.setOnClickListener {
            loadingProgressBar.visibility = View.VISIBLE
            loginAuthViewModel.login(
                emailEditText.text.toString(),
                passwordEditText.text.toString()
            )
        }
    }

    private fun showLoginFailed(@StringRes errorString: Int) {
        val appContext = applicationContext ?: return
        Toast.makeText(appContext, errorString, Toast.LENGTH_LONG).show()
    }
}
