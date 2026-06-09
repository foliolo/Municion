package al.ahgitdevelopment.municion.domain.usecase

import al.ahgitdevelopment.municion.data.local.room.MunicionDatabase
import al.ahgitdevelopment.municion.firebase.CrashReporter

/**
 * Clears ALL local data (used on sign-out so no data leaks between users in the
 * single-tenant database).
 */
class ClearLocalDataUseCase(
    private val database: MunicionDatabase,
    private val crashReporter: CrashReporter,
) {
    suspend operator fun invoke(): Result<Unit> = try {
        database.licenciaDao().deleteAll()
        database.guiaDao().deleteAll()
        database.compraDao().deleteAll()
        database.tiradaDao().deleteAll()
        database.appPurchaseDao().deleteAll()
        database.syncOperationDao().deleteAll()
        Result.success(Unit)
    } catch (e: Exception) {
        crashReporter.recordException(e)
        Result.failure(e)
    }
}
