package com.griffith.luckywheel.services

fun validatePassword(password: String, disable_string_validations: Boolean = false): String? {
    if (password.isBlank()) return "Password cannot be empty"
    if (!disable_string_validations){
        if (password.length < 8) return "Password must be at least 8 characters long"
        if (!password.any { it.isUpperCase() }) return "Password must contain at least one uppercase letter"
        if (!password.any { it.isLowerCase() }) return "Password must contain at least one lowercase letter"
        if (!password.any { it.isDigit() }) return "Password must contain at least one digit"
        if (!password.any { "!@#\$%^&*()-_=+[]{}|;:'\",.<>?/`~".contains(it) })
            return "Password must contain at least one special character"
    }
    return null // valid
}


fun validateEmail(email: String): String? {
    if (email.isBlank()) return "Email cannot be empty"
    val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}\$")
    if (!emailRegex.matches(email)) return "Invalid email format"

    return null // valid
}

