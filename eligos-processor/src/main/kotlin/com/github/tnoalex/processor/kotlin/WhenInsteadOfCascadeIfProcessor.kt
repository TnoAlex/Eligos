package com.github.tnoalex.processor.kotlin

import com.github.tnoalex.config.WiredConfig
import com.github.tnoalex.foundation.LaunchEnvironment
import com.github.tnoalex.foundation.bean.Component
import com.github.tnoalex.foundation.bean.Suitable
import com.github.tnoalex.foundation.eventbus.EventListener
import com.github.tnoalex.issues.kotlin.WhenInsteadOfCascadeIfIssue
import com.github.tnoalex.processor.PsiProcessor
import com.github.tnoalex.processor.utils.filePath
import com.github.tnoalex.processor.utils.startLine
import org.jetbrains.kotlin.KtNodeTypes
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtIfExpression
import org.jetbrains.kotlin.psi.KtTreeVisitorVoid

@Component
@Suitable(LaunchEnvironment.CLI)
class WhenInsteadOfCascadeIfProcessor : PsiProcessor {
    override val supportLanguage: List<String>
        get() = listOf("kotlin")

    @WiredConfig("expression.ifCascadeDepth")
    private var maxCascadeIfDepth = 0

    @EventListener
    fun process(ktFile: KtFile) {
        ktFile.accept(ifExpressionVisitor)
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