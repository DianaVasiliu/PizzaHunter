package com.android.example.pizzahunter

class Constants {
    object ERRORS {
        const val INVALID_CREDENTIALS = "E-mail sau parola incorecte"
        const val INVALID_PASSWORD = "Parola incorecta sau contul nu are parola"
        const val COULDNT_UPDATE_PASSWORD = "Parola nu a putut fi schimbata"
        const val USER_NOT_LOGGED_IN = "Utilizatorul nu este logat"
    }
    object USER_DB_KEYS {
        const val FIRST_NAME = "firstName"
        const val LAST_NAME = "lastName"
        const val EMAIL = "email"
        const val PHONE_NUMBER = "phoneNumber"
        const val PROFILE_PIC_URI = "profilePicUri"
    }
}