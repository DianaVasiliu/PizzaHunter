package com.android.example.pizzahunter

import android.content.Context
import android.opengl.Visibility
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.databinding.DataBindingUtil
import com.android.example.pizzahunter.databinding.ActivitySignupBinding
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.regex.Pattern
import kotlin.coroutines.CoroutineContext

class SignupActivity : AppCompatActivity(), CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        this.supportActionBar?.title = getString(R.string.signup_title)
        this.supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val binding = DataBindingUtil.setContentView<ActivitySignupBinding>(this, R.layout.activity_signup)

        binding.signupInnerContainer.setOnClickListener {
            hideKeyboard(it)
        }

        binding.signupButton.button.setOnClickListener {
            binding.loadingScreen.visibility = View.VISIBLE

            val formIsOk = validateForm(binding)

            if (formIsOk) {
                val firstName : String = binding.firstNameTextinput.text.toString()
                val lastName : String = binding.lastNameTextinput.text.toString()
                val email : String = binding.emailTextinput.text.toString()
                val phoneNumber : String = binding.phoneTextinput.text.toString()
                val password : String = binding.passwordTextinput.text.toString()

                launch {
                    val user = signUp(email, password)
                    if (user != null) {
                        // TODO: reset navigation stack
                        // TODO: reset account page UI (load the logged-in fragment)
                        val dbUser = hashMapOf(
                            "firstName" to firstName,
                            "lastName" to lastName,
                            "email" to email,
                            "phoneNumber" to phoneNumber
                        )
                        Database.addUser(user.uid, dbUser)
                        binding.loadingScreen.visibility = View.GONE
                        finish()
                    }
                    else {
                        binding.loadingScreen.visibility = View.GONE
                        showError(binding.errorText, R.string.something_went_wrong)
                        binding.signupScrollView.fullScroll(View.FOCUS_UP)
                    }
                }
            }
            else {
                binding.loadingScreen.visibility = View.GONE
                binding.signupScrollView.fullScroll(View.FOCUS_UP)
            }
        }
    }

    private fun showError(error: TextView, message: Int) {
        error.visibility = View.VISIBLE
        error.text = getString(message)
    }

    private fun validateForm(binding: ActivitySignupBinding): Boolean {
        val firstName : String = binding.firstNameTextinput.text.toString()
        val lastName : String = binding.lastNameTextinput.text.toString()
        val email : String = binding.emailTextinput.text.toString()
        val phoneNumber : String = binding.phoneTextinput.text.toString()
        val password : String = binding.passwordTextinput.text.toString()
        val confirmPassword : String = binding.confirmPasswordTextinput.text.toString()
        val checkbox = binding.termsAndConditionsCheckbox

        val error = binding.errorText

        fun isEmailValid(str: String): Boolean {
            return !TextUtils.isEmpty(str) && android.util.Patterns.EMAIL_ADDRESS.matcher(str).matches()
        }

        fun isPhoneNumberValid(str: String): Boolean {
            val pattern = Pattern.compile("[0][7][0-9]{8}")
            val matcher = pattern.matcher(str)
            return matcher.matches()
        }

        if (firstName.isEmpty()
            || lastName.isEmpty()
            || email.isEmpty()
            || phoneNumber.isEmpty()
            || password.isEmpty()
            || confirmPassword.isEmpty()) {
            showError(error, R.string.error_provide_all)
            return false
        }

        if (!isEmailValid(email)) {
            showError(error, R.string.error_invalid_email)
            return false
        }

        if (!isPhoneNumberValid(phoneNumber)) {
            showError(error, R.string.error_invalid_phone_number)
            return false
        }

        if (password.length < 6) {
            showError(error, R.string.error_short_password)
            return false
        }

        if (password != confirmPassword) {
            showError(error, R.string.error_passwords_dont_match)
            return false
        }

        if (!checkbox.isChecked) {
            showError(error, R.string.error_checkbox)
            return false
        }

        return true
    }

    private suspend fun signUp(email: String, password: String): FirebaseUser? {
        return Database.createUserWithEmailAndPassword(email, password)
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