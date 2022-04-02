package com.android.example.pizzahunter.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.databinding.DataBindingUtil
import com.android.example.pizzahunter.*
import com.android.example.pizzahunter.database.Database
import com.android.example.pizzahunter.databinding.ActivityEditProfileBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.regex.Pattern
import kotlin.coroutines.CoroutineContext

class EditProfileActivity : AppCompatActivity(), CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main

    private lateinit var binding : ActivityEditProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        this.supportActionBar?.title = getString(R.string.edit_profile_title)
        this.supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_edit_profile)

        binding.saveButton.button.setOnClickListener {
            binding.loadingScreen.visibility = View.VISIBLE
            var canFinish = true

            val isFormOk = validateForm()

            if (isFormOk) {
                val firstName : String = binding.firstNameTextInput.text.toString()
                val lastName : String = binding.lastNameTextInput.text.toString()
                val phoneNumber : String = binding.editTextPhone.text.toString()
                val oldPassword : String = binding.oldPasswordTextInput.text.toString()
                val newPassword : String = binding.newPasswordTextInput.text.toString()

                launch {
                    val newUserInfo = hashMapOf<String, Any?>()
                    if (firstName.isNotEmpty()) {
                        newUserInfo[Constants.USER_DB_KEYS.FIRST_NAME] = firstName
                    }
                    if (lastName.isNotEmpty()) {
                        newUserInfo[Constants.USER_DB_KEYS.LAST_NAME] = lastName
                    }
                    if (phoneNumber.isNotEmpty()) {
                        newUserInfo[Constants.USER_DB_KEYS.PHONE_NUMBER] = phoneNumber
                    }

                    if (newUserInfo.isNotEmpty()) {
                        Database.updateUser(newUserInfo)
                    }

                    if (oldPassword.isNotEmpty()) {
                        val error : String? = Database.updatePassword(oldPassword, newPassword)
                        if (error != null) {
                            showStringError(binding.errorText, error)
                            canFinish = false
                        }
                    }
                    if (canFinish) {
                        finish()
                    }
                }
            }

            binding.loadingScreen.visibility = View.GONE
        }
    }
    private fun showStringError(error: TextView, message: String) {
        error.visibility = View.VISIBLE
        error.text = message
        binding.scrollView.smoothScrollTo(0, 0)
    }

    private fun showError(error: TextView, message: Int) {
        error.visibility = View.VISIBLE
        error.text = getString(message)
        binding.scrollView.smoothScrollTo(0, 0)
    }

    private fun validateForm(): Boolean {
        val phoneNumber : String = binding.editTextPhone.text.toString()
        val oldPassword : String = binding.oldPasswordTextInput.text.toString()
        val newPassword : String = binding.newPasswordTextInput.text.toString()
        val confirmNewPassword : String = binding.confirmNewPasswordTextInput.text.toString()

        val error = binding.errorText

        fun isPhoneNumberValid(str: String): Boolean {
            val pattern = Pattern.compile("[0][7][0-9]{8}")
            val matcher = pattern.matcher(str)
            return matcher.matches()
        }

        if (oldPassword.isNotEmpty()) {
            if (newPassword.isEmpty() || confirmNewPassword.isEmpty()) {
                showError(error, R.string.error_provide_passwords)
                return false
            }
            if (newPassword != confirmNewPassword) {
                showError(error, R.string.error_passwords_dont_match)
                return false
            }
            if (newPassword.length < 6) {
                showError(error, R.string.error_short_password)
                return false
            }
        }

        if (phoneNumber.isNotEmpty()) {
            if (!isPhoneNumberValid(phoneNumber)) {
                showError(error, R.string.error_invalid_phone_number)
                return false
            }
        }

        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}