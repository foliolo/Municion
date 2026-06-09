package al.ahgitdevelopment.municion.firebase

import dev.gitlive.firebase.storage.Data

/** Wraps raw image bytes in GitLive's platform-specific [Data] for Firebase Storage uploads. */
expect fun ByteArray.toStorageData(): Data
