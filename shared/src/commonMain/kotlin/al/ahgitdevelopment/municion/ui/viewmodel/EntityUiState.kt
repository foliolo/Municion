package al.ahgitdevelopment.municion.ui.viewmodel

/** Shared one-shot UI state for entity CRUD operations. */
sealed interface EntityUiState {
    data object Idle : EntityUiState
    data object Loading : EntityUiState
    data class Success(val message: String) : EntityUiState
    data class Error(val message: String) : EntityUiState
}
