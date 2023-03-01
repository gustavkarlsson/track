@file:Suppress("UnstableApiUsage")

package se.gustavkarlsson.track.lint

import com.android.tools.lint.checks.infrastructure.LintDetectorTest
import com.android.tools.lint.checks.infrastructure.TestFiles.kotlin
import com.android.tools.lint.checks.infrastructure.TestMode
import org.intellij.lang.annotations.Language

// Instead of annotating tests, make sure each test function starts with "test"
internal class QueryReturnsSequenceDetectorTest : LintDetectorTest() {

    override fun getDetector() = QueryReturnsSequenceDetector()

    override fun getIssues() = listOf(QueryReturnsSequenceDetector.ISSUE)

    fun `test clean file`() {
        lint()
            .allowMissingSdk()
            .files(stubImplementation, cleanFile)
            .skipTestModes(TestMode.TYPE_ALIAS)
            .run()
            .expectClean()
    }

    fun `test dirty file using companion`() {
        lint()
            .allowMissingSdk()
            .files(stubImplementation, dirtyCompanionFile)
            .skipTestModes(TestMode.TYPE_ALIAS)
            .run()
            .expectWarningCount(2)
    }

    fun `test dirty file using custom instance`() {
        lint()
            .allowMissingSdk()
            .files(stubImplementation, dirtyCustomFile)
            .skipTestModes(TestMode.TYPE_ALIAS)
            .run()
            .expectWarningCount(2)
    }
}

// TODO Somehow replace this with actual source code
@Language("kotlin")
private val stubImplementation = kotlin(
    """
        package se.gustavkarlsson.track

        object Record

        class Track {
            suspend fun query(key: String): List<Record> = query(key) { it.toList() }
            suspend fun <T> query(key: String, selector: (Sequence<Record>) -> T): T {
                return emptySequence<Record>()
            }
            companion object : Track {
                suspend fun query(key: String): List<Record> = query(key) { it.toList() }
                suspend fun <T> query(key: String, selector: (Sequence<Record>) -> T): T {
                    return emptySequence<Record>()
                }
            }
        }
    """.trimIndent()
)

@Language("kotlin")
private val cleanFile = kotlin(
    """
        package se.gustavkarlsson.track

        // Other function with same signature
        fun <T> query(key: String, selector: (Sequence<Record>) -> T): T {
            return emptySequence<Record>()
        }

        suspend fun main() {
            Track.query("key")
            Track.query("key") { it.count() }
            Track.query("key") { it.toList() }
            query("key") { it }
        }
    """.trimIndent()
).indented()

@Language("kotlin")
private val dirtyCompanionFile = kotlin(
    """
        package se.gustavkarlsson.track

        fun main() {
            Track.query("key") { it }
            Track.query("key") { it.map { 1 } }
        }
    """.trimIndent()
).indented()

@Language("kotlin")
private val dirtyCustomFile = kotlin(
    """
        package se.gustavkarlsson.track

        fun main() {
            val track = Track()
            track.query("key") { it }
            track.query("key") { it.map { 1 } }
        }
    """.trimIndent()
).indented()
