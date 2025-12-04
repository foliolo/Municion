package al.ahgitdevelopment.municion.domain.usecase

import al.ahgitdevelopment.municion.data.local.room.MunicionDatabase
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Use Case to clear ALL local data (Room Database)
 *
 * Used when signing out to ensure no data leakage between users
 * in the single-tenant database architecture.
 */
class ClearLocalDataUseCase @Inject constructor(
    private val database: MunicionDatabase
) {
    suspend operator fun invoke() = withContext(Dispatchers.IO) {
        try {
            Log.i("ClearLocalDataUseCase", "Clearing all local tables...")
            database.clearAllTables()
            Log.i("ClearLocalDataUseCase", "Local database cleared successfully")
        } catch (e: Exception) {
            Log.e("ClearLocalDataUseCase", "Error clearing local database", e)
            throw e
        }
    }
}
