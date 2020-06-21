@file:Suppress("UnstableApiUsage")

package se.gustavkarlsson.track.lint

import com.android.tools.lint.client.api.IssueRegistry
import com.android.tools.lint.detector.api.CURRENT_API

@Suppress("unused")
internal class IssueRegistry : IssueRegistry() {

    override val api = CURRENT_API

    override val issues = listOf(QueryReturnsSequenceDetector.ISSUE)
}
