package al.ahgitdevelopment.municion.repository.preferences

interface SharedPreferencesContract {
    fun existUser(): Boolean
    fun getPassword(): String
    fun setPassword(password: String)
    fun getShowTutorial(): Boolean
    fun setShowTutorial(show: Boolean)
}
