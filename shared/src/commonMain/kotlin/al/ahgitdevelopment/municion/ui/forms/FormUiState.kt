package al.ahgitdevelopment.municion.ui.forms

/** Shared one-shot UI state for entity form screens. */
sealed interface FormUiState {
    data object Idle : FormUiState

    data object Saving : FormUiState

    data class Saved(
        val message: String,
    ) : FormUiState

    data class Error(
        val message: String,
    ) : FormUiState
}
