package com.android.example.pizzahunter

import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class Database {

    companion object {
        private const val CREATE_ACCOUNT_TAG = "CREATE_ACCOUNT"
        private const val LOGIN_TAG = "LOGIN"
        private const val FIREBASE_TAG = "FIREBASE"

        private var error: String = ""

        fun getError(): String {
            return error
        }

        private fun firestore() : FirebaseFirestore {
            return Firebase.firestore
        }

        private fun auth() : FirebaseAuth {
            return Firebase.auth
        }

        fun onAuthStateChange(callback: () -> Unit) {
            auth().addAuthStateListener {
                callback()
            }
        }

        fun checkUserLoggedIn() {
            auth().currentUser?.reload()
        }

        fun isUserLoggedIn(): Boolean {
            return auth().currentUser != null
        }

        suspend fun getUser(): MutableMap<String, Any>? {
            val db = firestore()
            val id = auth().currentUser?.uid
            return if (id != "" && id != null) {
                val result = db.collection("users").document(id).get().await()
                result.data
            } else {
                null
            }
        }

        suspend fun signInWithEmailAndPassword(email: String, password: String): FirebaseUser? {
            return withContext(Dispatchers.IO) {
                try {
                    val result = auth().signInWithEmailAndPassword(email, password).await()
                    result.user
                }
                catch (e: Exception) {
                    Log.d(LOGIN_TAG, e.toString())
                    error = Constants.ERRORS.INVALID_CREDENTIALS
                    null
                }
            }
        }

        suspend fun createUserWithEmailAndPassword(email: String, password: String): FirebaseUser? {
            return withContext(Dispatchers.IO) {
                try {
                    val result = auth().createUserWithEmailAndPassword(email, password).await()
                    result.user
                }
                catch (e: Exception) {
                    Log.d(CREATE_ACCOUNT_TAG, e.toString())
                    error = e.message.toString()
                    null
                }
            }
        }

        suspend fun googleLogin(account: GoogleSignInAccount?) {
            Log.d("GOOGLE_LOGIN", "googleLogin: begin firebase google login")

            try {
                val credential = GoogleAuthProvider.getCredential(account!!.idToken, null)
                val authResult = auth().signInWithCredential(credential).await()
                Log.d("GOOGLE_LOGIN", "googleLogin: success")

                val firebaseUser = auth().currentUser
                val email = firebaseUser!!.email
                val profilePicUri = firebaseUser.photoUrl.toString()

                val userName = firebaseUser.displayName
                var firstName = ""
                var lastName = ""

                if (userName!!.indexOf(' ') != -1) {
                    firstName = userName.substring(0, userName.indexOf(' '))
                    lastName = userName.substring(userName.indexOf(' ') + 1)
                } else {
                    firstName = userName
                    lastName = ""
                }

                if (authResult.additionalUserInfo!!.isNewUser) {
                    val userData = hashMapOf(
                        "firstName" to firstName,
                        "lastName" to lastName,
                        "email" to email!!,
                        "phoneNumber" to "",
                        "profilePicUri" to profilePicUri
                    )

                    addUser(firebaseUser.uid, userData)
                } else {
                    Log.d("GOOGLE_LOGIN", "googleLogin: Existing account ...")
                }
                error = ""

            } catch (e: Exception) {
                Log.d("GOOGLE_LOGIN", "googleLogin: Error logging in: ${e.message}")
                error = "Google Login Error: ${e.message.toString()}"
            }
        }

        fun signOut() {
            auth().signOut()
        }

        suspend fun addUser(id: String, data: HashMap<String, String>) {
            val dataOk = data.containsKey("firstName")
                    && data.containsKey("lastName")
                    && data.containsKey("email")
                    && data.containsKey("phoneNumber")

            if (!dataOk) {
                return
            }

            if (!data.containsKey("profilePicUri")) {
                data["profilePicUri"] = ""
            }

            try {
                val db = firestore()
                db.collection("users").document(id).set(data).await()
                Log.d(FIREBASE_TAG, "Successfully added $id")
            }
            catch (e: Exception) {
                Log.w(FIREBASE_TAG, "Failure", e)
            }
        }
    }
}