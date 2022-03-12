package com.android.example.pizzahunter

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.databinding.DataBindingUtil
import com.android.example.pizzahunter.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class LoginActivity : AppCompatActivity(), CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        this.supportActionBar?.title = getString(R.string.login_title)
        this.supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val binding = DataBindingUtil.setContentView<ActivityLoginBinding>(this, R.layout.activity_login)

        binding.loginInnerContainer.setOnClickListener {
            hideKeyboard(it)
        }

        binding.loginButton.button.setOnClickListener {
            binding.loadingScreen.visibility = View.VISIBLE

            val formIsOk = validateForm(binding)

            if (formIsOk) {
                val email : String = binding.emailTextinput.text.toString()
                val password : String = binding.passwordTextinput.text.toString()

                launch {
                    val user = logIn(email, password)
                    if (user != null) {
                        binding.loadingScreen.visibility = View.GONE
                        finish()
                    }
                    else {
                        binding.loadingScreen.visibility = View.GONE
                        showStringError(binding.errorText, Database.getError())//R.string.something_went_wrong)
                        binding.loginScrollView.fullScroll(View.FOCUS_UP)
                    }
                }
            }
            else {
                binding.loadingScreen.visibility = View.GONE
                binding.loginScrollView.fullScroll(View.FOCUS_UP)
            }
        }

    }
    private fun showStringError(error: TextView, message: String) {
        error.visibility = View.VISIBLE
        error.text = message
    }

    private fun showError(error: TextView, message: Int) {
        error.visibility = View.VISIBLE
        error.text = getString(message)
    }

    private fun validateForm(binding: ActivityLoginBinding): Boolean {
        val email : String = binding.emailTextinput.text.toString()
        val password : String = binding.passwordTextinput.text.toString()

        val error = binding.errorText

        if (email.isEmpty() || password.isEmpty()) {
            showError(error, R.string.error_provide_all)
            return false
        }

        return true
    }

    private suspend fun logIn(email: String, password: String): FirebaseUser? {
        return Database.signInWithEmailAndPassword(email, password)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun hideKeyboard(view: View) {
        val service = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        service.hideSoftInputFromWindow(view.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
    }

}