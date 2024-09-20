package com.github.tnoalex.processor.kotlin

import com.github.tnoalex.config.InjectConfig
import com.github.tnoalex.foundation.LaunchEnvironment
import com.github.tnoalex.foundation.bean.Component
import com.github.tnoalex.foundation.bean.Suitable
import com.github.tnoalex.foundation.eventbus.EventListener
import com.github.tnoalex.foundation.language.JavaLanguage
import com.github.tnoalex.foundation.language.KotlinLanguage
import com.github.tnoalex.foundation.language.Language
import com.github.tnoalex.issues.Severity
import com.github.tnoalex.issues.kotlin.WhenInsteadOfCascadeIfIssue
import com.github.tnoalex.processor.IssueProcessor
import com.github.tnoalex.processor.utils.filePath
import com.github.tnoalex.processor.utils.startLine
import com.intellij.psi.PsiFile
import org.jetbrains.kotlin.KtNodeTypes
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtIfExpression
import org.jetbrains.kotlin.psi.KtTreeVisitorVoid

@Component
@Suitable(LaunchEnvironment.CLI)
class WhenInsteadOfCascadeIfProcessor : IssueProcessor {
    override val severity: Severity
        get() = Severity.CODE_SMELL
    override val supportLanguage: List<Language>
        get() = listOf(KotlinLanguage)

    @InjectConfig("expression.ifCascadeDepth")
    private var maxCascadeIfDepth = 0

    @EventListener(filterClazz = [KtFile::class])
    override fun process(psiFile: PsiFile) {
        psiFile.accept(ifExpressionVisitor)
    }

    private val ifExpressionVisitor = object : KtTreeVisitorVoid() {
        override fun visitIfExpression(expression: KtIfExpression) {
            // if ... else if ..
            if (expression.parent.node.elementType == KtNodeTypes.ELSE) return super.visitIfExpression(expression)
            val cascadeDepth = maxCascadeDepth(expression) + 1
            if (cascadeDepth >= maxCascadeIfDepth) {
                context.reportIssue(
                    WhenInsteadOfCascadeIfIssue(
                        expression.filePath,
                        expression.text,
                        cascadeDepth,
                        expression.startLine
                    )
                )
            }
            super.visitIfExpression(expression)
        }
    }

    private fun maxCascadeDepth(expression: KtExpression?, depth: Int = 0): Int {
        if (expression == null || expression !is KtIfExpression) return depth
        return maxCascadeDepth(expression.`else`, depth + 1)
    }
}