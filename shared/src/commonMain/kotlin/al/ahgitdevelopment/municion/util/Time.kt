@file:OptIn(ExperimentalTime::class)

package al.ahgitdevelopment.municion.util

import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/** Multiplatform replacement for `System.currentTimeMillis()` (wall-clock epoch millis). */
fun nowMillis(): Long = Clock.System.now().toEpochMilliseconds()
