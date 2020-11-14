package al.ahgitdevelopment.municion.repository.preferences

// @EntryPoint
// @InstallIn(ApplicationComponent::class)
interface SharedPreferencesContract {
    fun existUser(): Boolean
    fun getPassword(): String
    fun setPassword(password: String)
    fun getShowTutorial(): Boolean
    fun setShowTutorial(show: Boolean)
}
