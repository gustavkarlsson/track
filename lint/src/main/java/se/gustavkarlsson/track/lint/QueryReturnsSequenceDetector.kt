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
import org.jetbrains.uast.UCallExpression
import java.util.EnumSet

// TODO some of these checks could certainly be more accurate
internal class QueryReturnsSequenceDetector : Detector(), SourceCodeScanner {

    override fun getApplicableMethodNames(): List<String> = listOf("query")

    override fun visitMethodCall(context: JavaContext, node: UCallExpression, method: PsiMethod) {
        super.visitMethodCall(context, node, method)
        if (!context.evaluator.isMemberInClass(method, "se.gustavkarlsson.track.Track.Companion")) return
        if (!method.hasTypeParameters()) return
        if (method.parameters.size != 2) return
        if ((method.parameters[0] as? PsiParameter)?.type?.canonicalText != "java.lang.String") return
        if ((method.parameters[1] as? PsiParameter)?.type?.canonicalText != "kotlin.jvm.functions.Function1<? super kotlin.sequences.Sequence<se.gustavkarlsson.track.Record>,? extends T>") return
        if (method.typeParameters.size != 1) return
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
            message = "Queries should not return sequences"
        )
    }

    companion object {
        val ISSUE = Issue.create(
            id = "QueryReturnsSequence",
            briefDescription = "Queries should not return sequences",
            explanation = """
                            The sequence of records must be exhausted within the lambda.
                            Returning a sequence is therefore a sign of a potential bug.
                            Tip: Consume the sequence by turning it into collection with toList() or similar.
                        """.trimIndent(),
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
