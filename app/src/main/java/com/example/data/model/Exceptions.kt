package com.example.data.model

open class VmlSecurityException(message: String) : Exception(message)

class InvalidCredentialsException(
    message: String = "Invalid email or password.",
    val attemptCount: Int = 1
) : VmlSecurityException(message)

class MaxAttemptsExceededException(
    message: String = "Maximum login attempts exceeded. Account has been locked.",
    val failedAttempts: Int = 2
) : VmlSecurityException(message)

class AccountLockedSecurityException(
    message: String = "Account is locked. Please contact system administrator.",
    val userId: String = "",
    val userEmail: String = "",
    val lockTimestamp: Long = System.currentTimeMillis()
) : VmlSecurityException(message)

class SystemLockdownException(
    message: String = "System is locked down for maintenance. Only Administrator can log in."
) : VmlSecurityException(message)

class UnauthorizedAccessException(
    message: String = "Access denied. You do not have permission to perform this action."
) : VmlSecurityException(message)

class InvalidPaymentException(
    message: String
) : VmlSecurityException(message)

class EmailNotVerifiedException(
    message: String = "Email verification required for first-time access.",
    val user: UserEntity? = null
) : VmlSecurityException(message)
