package com.mobilesystems.feedme

import android.content.Intent
import androidx.lifecycle.Observer
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.activity.viewModels
import com.mobilesystems.feedme.databinding.ActivityRegisterBinding
import com.mobilesystems.feedme.ui.authentication.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RegisterActivity : AppCompatActivity() {

    private val registerViewModel: AuthViewModel by viewModels()
    private var _binding: ActivityRegisterBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.alreadyHaveAccount.setOnClickListener {
            val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        val firstNameEditText = binding.inputFirstName
        val lastNameEditText = binding.inputLastName
        val emailEditText = binding.inputEmail
        val passwordEditText = binding.inputPassword
        val confirmPasswordEditText = binding.inputConfirmPassword
        val registerButton = binding.btnRegister
        val loadingProgressBar = binding.loadingRegister
        registerButton.isEnabled = false

        // To show back button in actionbar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        registerViewModel.registerFormState.observe(this,
            Observer { registerFormState ->
                if (registerFormState == null) {
                    return@Observer
                }
                registerButton.isEnabled = registerFormState.isDataValid
                registerFormState.usernameError?.let {
                    firstNameEditText.error = getString(it)
                }
                registerFormState.usernameError?.let {
                    lastNameEditText.error = getString(it)
                }
                registerFormState.emailError?.let {
                    emailEditText.error = getString(it)
                }
                registerFormState.confirmPasswordError?.let {
                    confirmPasswordEditText.error = getString(it)
                }
                registerFormState.passwordError?.let {
                    passwordEditText.error = getString(it)
                }
            })

        registerViewModel.registerResult.observe(this,
            Observer { registerResult ->
                registerResult ?: return@Observer
                loadingProgressBar.visibility = View.GONE
                registerResult.error?.let {
                    showLoginFailed(it)
                }
                registerResult.success?.let {
                    val intent = Intent(this@RegisterActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            })

        val afterTextChangedListener = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // ignore
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                // check Fields For Empty Values
                registerButton.isEnabled = (!(firstNameEditText.equals("") ||
                        lastNameEditText.equals("") ||
                        emailEditText.equals("") ||
                        passwordEditText.equals("") ||
                        confirmPasswordEditText.equals("")) &&
                        (passwordEditText != confirmPasswordEditText))
            }

            override fun afterTextChanged(s: Editable) {
                registerViewModel.observeRegisterDataChanged(
                    firstNameEditText.text.toString(),
                    lastNameEditText.text.toString(),
                    emailEditText.text.toString(),
                    passwordEditText.text.toString(),
                    confirmPasswordEditText.text.toString()
                )
            }
        }

        firstNameEditText.addTextChangedListener(afterTextChangedListener)
        lastNameEditText.addTextChangedListener(afterTextChangedListener)
        emailEditText.addTextChangedListener(afterTextChangedListener)
        confirmPasswordEditText.addTextChangedListener(afterTextChangedListener)
        passwordEditText.addTextChangedListener(afterTextChangedListener)

        confirmPasswordEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                registerViewModel.register(
                    firstNameEditText.text.toString(),
                    lastNameEditText.text.toString(),
                    emailEditText.text.toString(),
                    passwordEditText.text.toString(),
                    confirmPasswordEditText.text.toString()
                )
            }
            false
        }

        registerButton.setOnClickListener {
            loadingProgressBar.visibility = View.VISIBLE
            registerViewModel.register(
                firstNameEditText.text.toString(),
                lastNameEditText.text.toString(),
                emailEditText.text.toString(),
                passwordEditText.text.toString(),
                confirmPasswordEditText.text.toString()
            )
        }
    }

    private fun showLoginFailed(@StringRes errorString: Int) {
        val appContext = applicationContext ?: return
        Toast.makeText(appContext, errorString, Toast.LENGTH_LONG).show()
    }
}