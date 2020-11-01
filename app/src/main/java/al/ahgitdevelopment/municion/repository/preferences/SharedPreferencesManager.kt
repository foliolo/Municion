package al.ahgitdevelopment.municion.repository.preferences

import android.content.Context
import androidx.preference.PreferenceManager
import javax.inject.Inject

open class SharedPreferencesManager @Inject constructor(context: Context) : SharedPreferencesContract {

    private val prefs = PreferenceManager.getDefaultSharedPreferences(context)

    override fun existUser(): Boolean =
        prefs.contains(PREFS_PASSWORD) && prefs.getString(PREFS_PASSWORD, "")!!.isNotBlank()

    override fun getPassword(): String =
        prefs.getString(PREFS_PASSWORD, "") ?: ""

    override fun setPassword(password: String) =
        prefs.edit().apply { putString(PREFS_PASSWORD, password) }.apply()

    override fun getShowTutorial(): Boolean = prefs.getBoolean(PREFS_SHOW_TUTORIAL, true)

    override fun setShowTutorial(show: Boolean) =
        prefs.edit().apply { putBoolean(PREFS_PASSWORD, show) }.apply()

    companion object {
        const val PREFS_PASSWORD = "password"
        const val PREFS_SHOW_TUTORIAL = "tutorial"
    }
}
