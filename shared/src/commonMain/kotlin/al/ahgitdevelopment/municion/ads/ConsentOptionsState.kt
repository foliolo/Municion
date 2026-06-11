package al.ahgitdevelopment.municion.ads

/**
 * State for the "privacy options" entry point shown in Settings. [isAvailable] is true only when the
 * UMP privacy-options form is required/available for the user; [show] launches that form.
 */
class ConsentOptionsState(
    val isAvailable: Boolean,
    val show: () -> Unit,
)
