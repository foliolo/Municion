package al.ahgitdevelopment.municion.data.repository

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.io.File

/**
 * Locks in the **single most important guarantee** of the v3.3+ sync
 * subsystem at the source-code level:
 *
 *   "syncFromFirebase MUST NOT delete rows from Room, ever."
 *
 * The original catastrophic bug (Bugs #2 + #3 + #4 in
 * docs/SYNC_REDESIGN.md) was caused by the diff sync calling
 * `deleteLocal(...)` when a row was missing from the Firebase snapshot.
 * Reintroducing such a call — anywhere inside the `syncFromFirebase`
 * body of any repository — would re-open the door to the same data-loss
 * incident.
 *
 * This test scans the four repository sources and fails if it finds:
 *
 *   - `dao.delete*(...)` calls in `syncFromFirebase` (any form: `delete`,
 *     `deleteById`, `deleteAll`).
 *   - The legacy `fullSyncToFirebase` or `setValue(...)` calls on a
 *     collection root (the entry-point of the destructive auto-fix path).
 *
 * It is intentionally a string-based check rather than a runtime mock test:
 *
 *   - It runs in milliseconds with no Android/Hilt/Room/Firebase setup.
 *   - It catches the regression immediately during compile, regardless of
 *     whether a maintainer remembers to write a runtime test.
 *   - It is auditable by humans without re-running the suite.
 *
 * If you need to delete a row in response to a Firebase signal, do it via
 * the **tombstone** mechanism: upsert the row with `deleted = true`. The
 * UI already filters tombstones out of every query.
 */
@DisplayName("Sync non-destructive contract")
class SyncNonDestructiveContractTest {

    private val repositoryFiles = listOf(
        "LicenciaRepository.kt",
        "GuiaRepository.kt",
        "CompraRepository.kt",
        "TiradaRepository.kt"
    ).map { name -> File("src/main/kotlin/al/ahgitdevelopment/municion/data/repository/$name") }

    @Test
    @DisplayName("Every repository source file exists where expected")
    fun sourcesExist() {
        for (file in repositoryFiles) {
            assertTrue(file.exists(), "Expected file at ${file.absolutePath} (cwd=${File(".").absolutePath})")
        }
    }

    @Test
    @DisplayName("syncFromFirebase bodies never call dao.delete*()")
    fun syncFromFirebaseHasNoDeletes() {
        // Extract the entire body of suspend fun syncFromFirebase from each
        // repository file. Then assert no `Dao.delete...(`-shaped calls exist
        // inside.
        val deletePattern = Regex("""\b(licencia|guia|compra|tirada)Dao\.delete\w*\s*\(""")

        for (file in repositoryFiles) {
            val body = extractFunBody(file.readText(), "syncFromFirebase")
            assertTrue(body.isNotBlank(), "syncFromFirebase not found in ${file.name}")
            val hits = deletePattern.findAll(body).toList()
            assertEquals(
                0, hits.size,
                "${file.name}: syncFromFirebase must not call any dao.delete*() — " +
                    "use tombstones (copy(deleted=true)) instead. " +
                    "Found: ${hits.joinToString { it.value }}"
            )
        }
    }

    @Test
    @DisplayName("No repository CALLS fullSyncToFirebase (deleted in v3.3)")
    fun fullSyncToFirebaseIsGone() {
        // Only actual call sites (`.fullSyncToFirebase(...)`), not comments.
        val callPattern = Regex("""\.fullSyncToFirebase\s*\(""")
        for (file in repositoryFiles) {
            val text = file.readText()
            assertFalse(
                callPattern.containsMatchIn(text),
                "${file.name} calls fullSyncToFirebase — that method was deleted in v3.3 " +
                    "because it could wipe an entire Firebase collection in one call."
            )
        }
    }

    @Test
    @DisplayName("No repository calls setValue on a collection root")
    fun noCollectionLevelSetValue() {
        // Pattern: .child("<collection>").setValue( — the dangerous shape
        // that replaces an entire path. We allow .child("<syncId>").setValue(
        // because that's a per-entity write.
        val collectionLevelSetValue = Regex(
            """\.child\(\s*ENTITY_PATH\s*\)\s*\.setValue\("""
        )
        for (file in repositoryFiles) {
            val text = file.readText()
            assertFalse(
                collectionLevelSetValue.containsMatchIn(text),
                "${file.name} writes to a collection root via setValue — only per-syncId " +
                    "writes are allowed. Collection-level writes wiped data in v3.2.x."
            )
        }
    }

    /**
     * Returns the body of the first occurrence of `suspend fun <name>` in
     * the given source text. Bodies are tracked by counting braces, which
     * is good enough for Kotlin source that doesn't use braces inside
     * string templates of the function signature itself.
     */
    private fun extractFunBody(source: String, name: String): String {
        val signature = Regex("""suspend\s+fun\s+$name\b""")
        val match = signature.find(source) ?: return ""
        val start = source.indexOf('{', match.range.last)
        if (start < 0) return ""
        var depth = 0
        var i = start
        while (i < source.length) {
            when (source[i]) {
                '{' -> depth++
                '}' -> {
                    depth--
                    if (depth == 0) {
                        return source.substring(start + 1, i)
                    }
                }
            }
            i++
        }
        return ""
    }
}
