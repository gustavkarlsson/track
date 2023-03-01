@file:Suppress("UnstableApiUsage")

package se.gustavkarlsson.track.lint

import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.android.tools.lint.detector.api.SourceCodeScanner
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiParameter
import java.util.EnumSet
import org.jetbrains.uast.UCallExpression

// TODO some of these checks could certainly be more accurate
internal class QueryReturnsSequenceDetector : Detector(), SourceCodeScanner {

    override fun getApplicableMethodNames(): List<String> = listOf("query")

    // ktlint-disable max-line-length
    override fun visitMethodCall(context: JavaContext, node: UCallExpression, method: PsiMethod) {
        super.visitMethodCall(context, node, method)
        if (!context.evaluator.isMemberInSubClassOf(method, "se.gustavkarlsson.track.Track")) return
        if (method.name != "query") return
        if (method.parameters.size != 3) return
        if ((method.parameters[0] as? PsiParameter)?.type?.canonicalText != "java.lang.String") return
        if ((method.parameters[1] as? PsiParameter)?.type?.canonicalText != "kotlin.jvm.functions.Function1<? super kotlin.sequences.Sequence<se.gustavkarlsson.track.Record>,? extends T>") return
        if ((method.parameters[2] as? PsiParameter)?.type?.canonicalText != "kotlin.coroutines.Continuation<? super T>") return
        if (node.returnType?.canonicalText?.startsWith("kotlin.sequences") == false) return
        reportIssue(context, node)
    }

    private fun reportIssue(context: JavaContext, node: UCallExpression) {
        context.report(
            issue = ISSUE,
            scope = node,
            location = context.getCallLocation(
                call = node,
                includeReceiver = true,
                includeArguments = true
            ),
            message = "Returning a sequence from the query lambda may leak database cursors. " +
                "Consume the sequence by turning it into collection with `toList()` or similar."
        )
    }

    companion object {
        val ISSUE = Issue.create(
            id = "QueryReturnsSequence",
            briefDescription = "Query returns sequence",
            explanation = buildString {
                append("The sequence of records in the query lambda is backed by a database cursor")
                append(" that will be closed when the lambda returns.")
                append(" Returning this sequence (or one derived from it) will make the sequence unusable.")
                append(" Tip: Consume the sequence by turning it into collection with `toList()` or similar.")
            },
            category = Category.CORRECTNESS,
            priority = 4,
            severity = Severity.WARNING,
            implementation = Implementation(
                QueryReturnsSequenceDetector::class.java,
                EnumSet.of(Scope.JAVA_FILE, Scope.TEST_SOURCES),
                EnumSet.of(Scope.JAVA_FILE),
                EnumSet.of(Scope.TEST_SOURCES)
            )
        )
    }
}
