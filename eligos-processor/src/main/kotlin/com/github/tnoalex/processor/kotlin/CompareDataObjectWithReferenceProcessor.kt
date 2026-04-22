package com.github.tnoalex.processor.kotlin

import com.github.tnoalex.foundation.LaunchEnvironment
import com.github.tnoalex.foundation.bean.Component
import com.github.tnoalex.foundation.bean.Suitable
import com.github.tnoalex.foundation.eventbus.EventListener
import com.github.tnoalex.foundation.language.KotlinLanguage
import com.github.tnoalex.foundation.language.Language
import com.github.tnoalex.issues.Severity
import com.github.tnoalex.issues.kotlin.CompareDataObjectWithReferenceIssue
import com.github.tnoalex.processor.IssueProcessor
import com.github.tnoalex.processor.utils.filePath
import com.github.tnoalex.processor.utils.startLine
import com.intellij.psi.PsiFile
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.symbols.KaClassSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaVariableSymbol
import org.jetbrains.kotlin.analysis.api.types.symbol
import org.jetbrains.kotlin.idea.references.mainReference
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtTreeVisitorVoid
import org.slf4j.LoggerFactory

@Component
@Suitable(LaunchEnvironment.CLI)
class CompareDataObjectWithReferenceProcessor : IssueProcessor {
    override val severity: Severity
        get() = Severity.CODE_SMELL
    override val supportLanguage: List<Language>
        get() = listOf(KotlinLanguage)

    @EventListener(filterClazz = [KtFile::class])
    override fun process(psiFile: PsiFile) {
        psiFile.accept(compareExpressionVisitor)
    }

    private val compareExpressionVisitor = object : KtTreeVisitorVoid() {
        override fun visitBinaryExpression(expression: KtBinaryExpression) {
            val left = expression.left ?: return super.visitBinaryExpression(expression)
            val operator = expression.operationToken
            if (operator != KtTokens.EQEQEQ) {
                return super.visitBinaryExpression(expression)
            }
            val right = expression.right ?: return super.visitBinaryExpression(expression)

            val leftRef = getTargetIfIsDataObject(left)
            val rightRef = getTargetIfIsDataObject(right)
            if (leftRef != null && rightRef != null) {
                context.reportIssue(
                    CompareDataObjectWithReferenceIssue(
                        expression.filePath,
                        expression.text,
                        leftRef.name.asString(),
                        rightRef.name.asString(),
                        expression.startLine
                    )
                )
            }
            super.visitBinaryExpression(expression)
        }
    }

    private fun getTargetIfIsDataObject(expr: KtExpression): KaVariableSymbol? {
        analyze(expr) {
            val ref = expr.mainReference ?: return null
            val symbol = ref.resolveToSymbol()
            if (symbol !is KaVariableSymbol) return null
            val typeSymbol = symbol.returnType.symbol as? KaClassSymbol ?: return null
            if (typeSymbol.classKind.isObject) {
                return symbol
            }
            return null
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(CompareDataObjectWithReferenceProcessor::class.java)
    }
}