package com.peter.auth

import io.ktor.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

@KtorExperimentalAPI // 1
val hashKey = hex(System.getenv("SECRET_KEY")) // 2

@KtorExperimentalAPI
val hmacKey = SecretKeySpec(hashKey, "HmacSHA1") // 3

@KtorExperimentalAPI
fun hash(password: String): String { // 4
    val hmac = Mac.getInstance("HmacSHA1")
    hmac.init(hmacKey)
    return hex(hmac.doFinal(password.toByteArray(Charsets.UTF_8)))
}