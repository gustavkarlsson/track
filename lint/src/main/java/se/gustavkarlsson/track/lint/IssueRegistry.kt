package se.gustavkarlsson.track.lint

import com.android.tools.lint.client.api.IssueRegistry
import com.android.tools.lint.client.api.Vendor
import com.android.tools.lint.detector.api.CURRENT_API

internal class IssueRegistry : IssueRegistry() {

    override val vendor = Vendor("Gustav Karlsson")

    override val api = CURRENT_API

    override val issues = listOf(QueryReturnsSequenceDetector.ISSUE)
}
