package com.peter.data

import java.io.Serializable

data class UserData(
    val userId: Int,
    val email: String,
    val displayName: String
) : Serializable
