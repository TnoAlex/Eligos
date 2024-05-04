package com.github.tnoalex.processor.kotlin

import com.github.tnoalex.foundation.LaunchEnvironment
import com.github.tnoalex.foundation.bean.Component
import com.github.tnoalex.foundation.bean.Suitable
import com.github.tnoalex.foundation.eventbus.EventListener
import com.github.tnoalex.issues.Severity
import com.github.tnoalex.issues.kotlin.CompareDataObjectWithReferenceIssue
import com.github.tnoalex.processor.PsiProcessor
import com.github.tnoalex.processor.utils.filePath
import com.github.tnoalex.processor.utils.nameCanNotResolveWarn
import com.github.tnoalex.processor.utils.resolveToDescriptorIfAny
import com.github.tnoalex.processor.utils.startLine
import com.intellij.psi.PsiReference
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.buildPossiblyInnerType
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameOrNull
import org.jetbrains.kotlin.utils.addToStdlib.ifFalse
import org.slf4j.LoggerFactory

@Component
@Suitable(LaunchEnvironment.CLI)
class CompareDataObjectWithReferenceProcessor : PsiProcessor {
    override val severity: Severity
        get() = Severity.CODE_SMELL
    override val supportLanguage: List<String>
        get() = listOf("kotlin")

    @EventListener
    fun process(ktFile: KtFile) {
        ktFile.accept(compareExpressionVisitor)
    }

    private val compareExpressionVisitor = object : KtTreeVisitorVoid() {
        override fun visitBinaryExpression(expression: KtBinaryExpression) {
            if (expression.children.size != 3) return super.visitBinaryExpression(expression)
            val (left, operator, right) = expression.children
            ((operator as KtOperationReferenceExpression).operationSignTokenType == KtTokens.EQEQEQ).ifFalse {
                return super.visitBinaryExpression(
                    expression
                )
            }

            val leftRef = isDataObject(left.references)
            val rightRef = isDataObject(right.references)
            if (leftRef != null && rightRef != null) {
                context.reportIssue(
                    CompareDataObjectWithReferenceIssue(
                        expression.filePath,
                        expression.text,
                        leftRef.resolveToDescriptorIfAny()?.fqNameOrNull()?.asString() ?: let {
                            logger.nameCanNotResolveWarn("property", left)
                            "Unknown property fqname"
                        },
                        rightRef.resolveToDescriptorIfAny()?.fqNameOrNull()?.asString() ?: let {
                            logger.nameCanNotResolveWarn("property", right)
                            "Unknown property fqname"
                        },
                        expression.startLine
                    )
                )
            }
            super.visitBinaryExpression(expression)
        }
    }

    private fun isDataObject(it: Array<PsiReference>): KtProperty? {
        it.forEach {
            val element = it.resolve()
            if (element !is KtProperty) {
                return@forEach
            }
            val classDescriptor =
                element.resolveToDescriptorIfAny()?.type?.buildPossiblyInnerType()?.classDescriptor
            if (classDescriptor != null && classDescriptor.kind == ClassKind.OBJECT && classDescriptor.isData) {
                return element
            }
        }
        return null
    }

    companion object {
        private val logger = LoggerFactory.getLogger(CompareDataObjectWithReferenceProcessor::class.java)
    }
}