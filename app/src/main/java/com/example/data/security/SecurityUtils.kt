package com.example.data.security

import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object SecurityUtils {
    private val SECURE_RANDOM = SecureRandom()
    private const val PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA256"
    private const val ITERATION_COUNT = 10000
    private const val KEY_LENGTH = 256

    fun generateSalt(): String {
        val salt = ByteArray(16)
        SECURE_RANDOM.nextBytes(salt)
        return Base64.encodeToString(salt, Base64.NO_WRAP)
    }

    fun generateEmailVerificationCode(): String {
        val code = 100000 + SECURE_RANDOM.nextInt(900000)
        return code.toString()
    }

    fun hashPassword(password: String, salt: String): String {
        val saltBytes = try {
            Base64.decode(salt, Base64.NO_WRAP)
        } catch (e: Throwable) {
            salt.toByteArray(Charsets.UTF_8)
        }
        val spec = PBEKeySpec(password.toCharArray(), saltBytes, ITERATION_COUNT, KEY_LENGTH)
        val factory = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM)
        val hash = factory.generateSecret(spec).encoded
        return Base64.encodeToString(hash, Base64.NO_WRAP)
    }

    fun verifyPassword(providedPassword: String, storedHash: String, salt: String): Boolean {
        if (providedPassword.isBlank() || storedHash.isBlank() || salt.isBlank()) return false
        val computedHash = hashPassword(providedPassword, salt)
        return MessageDigest.isEqual(
            computedHash.toByteArray(Charsets.UTF_8),
            storedHash.toByteArray(Charsets.UTF_8)
        )
    }

    fun verifyCodeConstantTime(providedCode: String, expectedCode: String): Boolean {
        if (providedCode.isBlank() || expectedCode.isBlank()) return false
        return MessageDigest.isEqual(
            providedCode.trim().toByteArray(Charsets.UTF_8),
            expectedCode.trim().toByteArray(Charsets.UTF_8)
        )
    }

    fun maskEmail(email: String): String {
        val atIndex = email.indexOf('@')
        if (atIndex <= 1) return "***" + email.substring(atIndex.coerceAtLeast(0))
        val prefix = email.substring(0, 1)
        val domain = email.substring(atIndex)
        return "$prefix***$domain"
    }

    fun sanitizeInput(input: String): String {
        return input.trim().replace(Regex("[<>&\"']"), "")
    }
}
