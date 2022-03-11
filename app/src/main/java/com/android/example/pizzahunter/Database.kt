package com.android.example.pizzahunter

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
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
        private const val FIREBASE_TAG = "FIREBASE"

        private fun firestore() : FirebaseFirestore {
            return Firebase.firestore
        }

        private fun auth() : FirebaseAuth {
            return Firebase.auth
        }

        fun checkUserLoggedIn() {
            auth().currentUser?.reload()
        }

        suspend fun createUserWithEmailAndPassword(email: String, password: String): FirebaseUser? {
            return withContext(Dispatchers.IO) {
                try {
                    val result = auth().createUserWithEmailAndPassword(email, password).await()
                    result.user
                }
                catch (e: Exception) {
                    Log.d(CREATE_ACCOUNT_TAG, e.toString())
                    null
                }
            }
        }

        suspend fun addUser(id: String, data: HashMap<String, String>) {
            val dataOk = data.containsKey("firstName")
                    && data.containsKey("lastName")
                    && data.containsKey("email")
                    && data.containsKey("phoneNumber")

            if (!dataOk) {
                return
            }

            try {
                val db = firestore()
                val result = db.collection("users").document(id).set(data).await()
                Log.d(FIREBASE_TAG, "Successfully added $id")
            }
            catch (e: Exception) {
                Log.w(FIREBASE_TAG, "Failure", e)
            }
        }
    }
}