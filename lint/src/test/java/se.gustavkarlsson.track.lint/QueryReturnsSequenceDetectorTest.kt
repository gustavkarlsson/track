@file:Suppress("UnstableApiUsage")

package se.gustavkarlsson.track.lint

import com.android.tools.lint.checks.infrastructure.LintDetectorTest
import com.android.tools.lint.checks.infrastructure.TestFiles.kotlin
import org.intellij.lang.annotations.Language

// Instead of annotating tests, make sure each test function starts with "test"
internal class QueryReturnsSequenceDetectorTest : LintDetectorTest() {

    override fun getDetector() = QueryReturnsSequenceDetector()

    override fun getIssues() = listOf(QueryReturnsSequenceDetector.ISSUE)

    fun `test clean file`() {
        lint()
            .allowMissingSdk()
            .files(stubImplementation, cleanFile)
            .run()
            .expectClean()
    }

    fun `test dirty file`() {
        lint()
            .allowMissingSdk()
            .files(stubImplementation, dirtyFile)
            .run()
            .expectWarningCount(1)
    }
}

// TODO Somehow replace this with actual source code
@Language("kotlin")
private val stubImplementation = kotlin(
    """
        package se.gustavkarlsson.track

        object Record

        class Track {
            companion object {
                fun query(key: String): List<Record> = query(key) { it.toList() }
                fun <T> query(key: String, selector: (Sequence<Record>) -> T): T {
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

        fun main() {
            Track.query("key")
            Track.query("key") { it.count() }
            Track.query("key") { it.toList() }
            query("key") { it }
        }
    """.trimIndent()
).indented()

@Language("kotlin")
private val dirtyFile = kotlin(
    """
        package se.gustavkarlsson.track

        fun main() {
            Track.query("key") { it }
        }
    """.trimIndent()
).indented()
