package com.peter.data

import io.ktor.auth.*
import java.io.Serializable

data class User(
    val userId: Int,
    val email: String,
    val displayName: String,
    val passwordHash: String
) : Serializable, Principal {

    fun responseUserData() = UserData(userId, email, displayName)
}
