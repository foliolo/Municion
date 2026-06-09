package al.ahgitdevelopment.municion.analytics

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.analytics.analytics

/** [AnalyticsTracker] backed by GitLive Firebase Analytics. */
class FirebaseAnalyticsTracker : AnalyticsTracker {

    private val analytics get() = Firebase.analytics

    override fun setEnabled(enabled: Boolean) {
        analytics.setAnalyticsCollectionEnabled(enabled)
    }

    override fun setUserId(userId: String?) {
        analytics.setUserId(userId)
    }

    override fun logEvent(name: String, params: Map<String, Any?>) {
        val clean = buildMap<String, Any> {
            params.forEach { (key, value) ->
                when (value) {
                    is String -> put(key, value)
                    is Int -> put(key, value.toLong())
                    is Long -> put(key, value)
                    is Double -> put(key, value)
                    is Boolean -> put(key, value)
                    else -> Unit
                }
            }
        }
        analytics.logEvent(name, clean)
    }

    override fun logScreenView(screenName: String, screenClass: String?) {
        logEvent(
            "screen_view",
            buildMap {
                put("screen_name", screenName)
                if (screenClass != null) put("screen_class", screenClass)
            },
        )
    }
}
