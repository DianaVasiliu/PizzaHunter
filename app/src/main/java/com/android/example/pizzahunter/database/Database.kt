package com.android.example.pizzahunter.database

import android.net.Uri
import android.util.Log
import com.android.example.pizzahunter.Constants
import com.facebook.AccessToken
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
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

        private fun storage() : FirebaseStorage {
            return Firebase.storage
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
            val TAG = "GOOGLE_LOGIN"

            try {
                val credential = GoogleAuthProvider.getCredential(account!!.idToken, null)
                val authResult = auth().signInWithCredential(credential).await()
                Log.d(TAG, "googleLogin: success")

                val userData = getUserData()

                val firebaseUser = auth().currentUser

                if (authResult.additionalUserInfo!!.isNewUser) {
                    addUser(firebaseUser!!.uid, userData)
                }
                else {
                    Log.d(TAG, "googleLogin: Existing account ...")
                }
                error = ""

            } catch (e: Exception) {
                Log.d(TAG, "googleLogin: Error logging in: ${e.message}")
                error = "Google Login Error: ${e.message.toString()}"
            }
        }

        suspend fun facebookLogin(token: AccessToken) {
            val TAG = "FACEBOOK_LOGIN"

            try {
                val credential = FacebookAuthProvider.getCredential(token.token)
                val authResult = auth().signInWithCredential(credential).await()

                Log.d(TAG, "signInWithCredential:success")

                val userData = getUserData()

                val firebaseUser = auth().currentUser

                if (authResult.additionalUserInfo!!.isNewUser) {
                    addUser(firebaseUser!!.uid, userData)
                }
                else {
                    Log.d(TAG, "facebookLogin: Existing account ...")
                }
                error = ""
            }
            catch (e: Exception) {
                Log.d(TAG, "facebookLogin: ${e.message}")
            }
        }

        fun signOut() {
            auth().signOut()
        }

        suspend fun addUser(id: String, data: HashMap<String, String>) {
            val dataOk = data.containsKey(Constants.USER_DB_KEYS.FIRST_NAME)
                    && data.containsKey(Constants.USER_DB_KEYS.LAST_NAME)
                    && data.containsKey(Constants.USER_DB_KEYS.EMAIL)
                    && data.containsKey(Constants.USER_DB_KEYS.PHONE_NUMBER)

            if (!dataOk) {
                return
            }

            if (!data.containsKey(Constants.USER_DB_KEYS.PROFILE_PIC_URI)) {
                data[Constants.USER_DB_KEYS.PROFILE_PIC_URI] = ""
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

        private fun getUserData(): HashMap<String, String> {
            val firebaseUser = auth().currentUser
            val email = firebaseUser!!.email
            val profilePicUri = firebaseUser.photoUrl.toString()

            val userName = firebaseUser.displayName
            val firstName: String
            val lastName: String

            if (userName!!.indexOf(' ') != -1) {
                firstName = userName.substring(0, userName.indexOf(' '))
                lastName = userName.substring(userName.indexOf(' ') + 1)
            } else {
                firstName = userName
                lastName = ""
            }

            return hashMapOf(
                Constants.USER_DB_KEYS.FIRST_NAME to firstName,
                Constants.USER_DB_KEYS.LAST_NAME to lastName,
                Constants.USER_DB_KEYS.EMAIL to email!!,
                Constants.USER_DB_KEYS.PHONE_NUMBER to "",
                Constants.USER_DB_KEYS.PROFILE_PIC_URI to profilePicUri
            )
        }

        suspend fun updateUser(data: HashMap<String, Any?>) {
            try {
                val db = firestore()
                val id = auth().currentUser?.uid.toString()
                db.collection("users").document(id).update(data).await()
                Log.d(FIREBASE_TAG, "Successfully updated $id")
            }
            catch (e: Exception) {
                Log.w(FIREBASE_TAG, "Failure", e)
            }
        }

        suspend fun loadProfilePicture(uri: Uri?) {
            if (uri == null) {
                return
            }

            try {
                val storageRef = storage().reference
                val profilePicRef = storageRef.child("profilePics/${auth().currentUser?.uid}")
                profilePicRef.putFile(uri).await()
                Log.d(FIREBASE_TAG, "Successfully uploaded file")
            }
            catch (e: Exception) {
                Log.d(FIREBASE_TAG, "Failed uploading file: ${e.message}")
            }
        }

        suspend fun getProfilePictureUri(): Uri? {
            return try {
                val storageRef = storage().reference
                val profilePicRef = storageRef.child("profilePics/${auth().currentUser?.uid}")
                profilePicRef.downloadUrl.await()
            } catch (e: Exception) {
                null
            }
        }

        suspend fun updatePassword(oldPassword: String, newPassword: String): String? {
            val user = auth().currentUser ?: return Constants.ERRORS.USER_NOT_LOGGED_IN
            var error : String? = null

            try {
                val credential = EmailAuthProvider.getCredential(user.email.toString(), oldPassword)
                user.reauthenticate(credential).await()
                user.updatePassword(newPassword).await()
            }
            catch (e: FirebaseAuthInvalidCredentialsException) {
                error = Constants.ERRORS.INVALID_PASSWORD
            }
            catch (e: Exception) {
                error = Constants.ERRORS.COULDNT_UPDATE_PASSWORD
            }
            return error
        }
    }
}