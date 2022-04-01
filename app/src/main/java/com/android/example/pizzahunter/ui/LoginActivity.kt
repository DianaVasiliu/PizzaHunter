package com.android.example.pizzahunter.ui

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.databinding.DataBindingUtil
import com.android.example.pizzahunter.database.Database
import com.android.example.pizzahunter.R
import com.android.example.pizzahunter.databinding.ActivityLoginBinding
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import kotlin.coroutines.CoroutineContext

class LoginActivity : AppCompatActivity(), CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main

    private lateinit var callbackManager: CallbackManager
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>

    private lateinit var binding: ActivityLoginBinding

    companion object {
        private const val FACEBOOK_LOGIN = "FACEBOOK_LOGIN"
        private const val GOOGLE_LOGIN = "GOOGLE_LOGIN"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        this.supportActionBar?.title = getString(R.string.login_title)
        this.supportActionBar?.setDisplayHomeAsUpEnabled(true)

        callbackManager = CallbackManager.Factory.create()

        LoginManager.getInstance().registerCallback(callbackManager, object: FacebookCallback<LoginResult> {
            override fun onCancel() {
                Log.d(FACEBOOK_LOGIN, "onCancel")
            }

            override fun onError(error: FacebookException) {
                Log.d(FACEBOOK_LOGIN, "onError: ${error.message}")
            }

            override fun onSuccess(result: LoginResult) {
                launch {
                    binding.loadingScreen.visibility = View.VISIBLE
                    Database.facebookLogin(result.accessToken)
                    if (Database.getError() == "") {
                        finish()
                    }
                    else {
                        binding.loadingScreen.visibility = View.GONE
                    }
                }
            }
        })

        val gso: GoogleSignInOptions =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .requestProfile()
                .build()

        val googleSignInClient = GoogleSignIn.getClient(this, gso)

        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()
        ) { result ->
            val resultCode = result!!.resultCode
            val data = result.data

            if (resultCode == RESULT_OK) {
                val accountTask = GoogleSignIn.getSignedInAccountFromIntent(data)

                try {
                    val account = accountTask.getResult(ApiException::class.java)

                    launch {
                        binding.loadingScreen.visibility = View.VISIBLE
                        Database.googleLogin(account)
                        if (Database.getError() == "") {
                            finish()
                        } else {
                            binding.loadingScreen.visibility = View.GONE
                        }
                    }

                } catch (e: Exception) {
                    Log.d(GOOGLE_LOGIN, "onActivityResult: ${e.message}")
                }
            } else {
                binding.loadingScreen.visibility = View.GONE
            }
        }

        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)

        binding.facebookButton.setOnClickListener {
            LoginManager.getInstance().logInWithReadPermissions(this, callbackManager, listOf("email", "public_profile"))
        }

        binding.googleButton.setOnClickListener {
            binding.loadingScreen.visibility = View.VISIBLE
            val intent = googleSignInClient.signInIntent
            activityResultLauncher.launch(intent)
        }

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