package com.griffith.luckywheel.utils

fun validatePassword(password: String, disableStringValidations: Boolean = false): String? {
    if (password.isBlank()) return "Password cannot be empty"
    if (!disableStringValidations) {
        if (password.length < 8) return "Password must be at least 8 characters long"
        if (!password.any { it.isUpperCase() }) return "Password must contain at least one uppercase letter"
        if (!password.any { it.isLowerCase() }) return "Password must contain at least one lowercase letter"
        if (!password.any { it.isDigit() }) return "Password must contain at least one digit"
    }
    return null
}

fun validateEmail(email: String): String? {
    if (email.isBlank()) return "Email cannot be empty"
    val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")
    if (!emailRegex.matches(email)) return "Invalid email format"
    return null
}

// Truncates text to maxLength and adds ellipsis (...) if text exceeds the limit
fun truncateText(text: String, maxLength: Int = 12): String {
    return if (text.length > maxLength) {
        text.take(maxLength) + "..."
    } else {
        text
    }
}
