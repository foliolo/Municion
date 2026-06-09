package al.ahgitdevelopment.municion.firebase

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth

/** Supplies the current Firebase Auth uid. Injected so tests can stub it without Firebase. */
fun interface CurrentUserIdProvider {
    fun currentUserId(): String?
}

class FirebaseCurrentUserIdProvider : CurrentUserIdProvider {
    override fun currentUserId(): String? = Firebase.auth.currentUser?.uid
}
