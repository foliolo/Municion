package al.ahgitdevelopment.municion.util

private val EMAIL_REGEX = Regex("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")

/** Multiplatform replacement for android.util.Patterns.EMAIL_ADDRESS. */
fun isValidEmail(email: String): Boolean = EMAIL_REGEX.matches(email.trim())
