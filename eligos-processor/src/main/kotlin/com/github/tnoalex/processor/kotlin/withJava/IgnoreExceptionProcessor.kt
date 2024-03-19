package com.github.tnoalex.processor.kotlin.withJava

import com.github.tnoalex.foundation.LaunchEnvironment
import com.github.tnoalex.foundation.bean.Component
import com.github.tnoalex.foundation.bean.Suitable
import com.github.tnoalex.foundation.eventbus.EventListener
import com.github.tnoalex.issues.kotlin.withJava.IgnoreExceptionIssue
import com.github.tnoalex.processor.PsiProcessor
import com.github.tnoalex.processor.utils.resolveToDescriptorIfAny
import com.github.tnoalex.processor.utils.startLine
import com.intellij.psi.PsiMethod
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.referenceExpression
import org.jetbrains.kotlin.utils.ifEmpty
import org.slf4j.LoggerFactory

@Component
@Suitable(LaunchEnvironment.CLI)
class IgnoreExceptionProcessor : PsiProcessor {
    override val supportLanguage: List<String>
        get() = listOf("kotlin", "java")

    @EventListener
    fun process(ktFile: KtFile) {
        ktFile.accept(ktCallExpressionVisitor)
    }

    private val ktCallExpressionVisitor = object : KtTreeVisitorVoid() {
        override fun visitCallExpression(expression: KtCallExpression) {
            if (annotatedWithThrows(expression)) return
            if (PsiTreeUtil.getParentOfType(expression, KtTryExpression::class.java) != null) return
            expression.referenceExpression()?.let {
                it.references.forEach { psi ->
                    try {
                        psi.resolve()?.let { r ->
                            if (r !is PsiMethod) return@forEach
                            val throws =
                                r.throwsList.referencedTypes.ifEmpty { return@forEach }.map { t -> t.className }
                            context.reportIssue(
                                IgnoreExceptionIssue(
                                    expression.containingFile.virtualFile.path,
                                    expression.text,
                                    throws,
                                    expression.startLine
                                )
                            )
                        }
                    } catch (e: RuntimeException) {
                        logger.warn(
                            "Can not resolve reference in file ${expression.containingFile.virtualFile.path}," +
                                    "line ${expression.startLine}"
                        )
                    }
                }
            }
            super.visitCallExpression(expression)
        }
    }

    private fun annotatedWithThrows(expression: KtCallExpression): Boolean {
        PsiTreeUtil.getParentOfType(expression, KtNamedFunction::class.java)?.let {
            it.resolveToDescriptorIfAny()?.run {
                if (annotations.findAnnotation(THROWS_FQ_NAME) != null) return true
            }
        } ?: PsiTreeUtil.getParentOfType(expression, KtConstructor::class.java)?.let {
            it.resolveToDescriptorIfAny()?.run {
                if (annotations.findAnnotation(THROWS_FQ_NAME) != null) return true
            }
        } ?: PsiTreeUtil.getParentOfType(expression, KtPropertyAccessor::class.java)?.let {
            it.resolveToDescriptorIfAny()?.run {
                if (annotations.findAnnotation(THROWS_FQ_NAME) != null) return true
            }
        } ?: return false
        return false
    }

    companion object {
        private val logger = LoggerFactory.getLogger(IgnoreExceptionProcessor::class.java)
        private val THROWS_FQ_NAME = FqName("kotlin.jvm.Throws")
    }
}