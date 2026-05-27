package com.github.tnoalex.processor.kotlin.withJava

import com.github.tnoalex.foundation.LaunchEnvironment
import com.github.tnoalex.foundation.bean.Component
import com.github.tnoalex.foundation.bean.Suitable
import com.github.tnoalex.foundation.eventbus.EventListener
import com.github.tnoalex.foundation.language.JavaLanguage
import com.github.tnoalex.foundation.language.KotlinLanguage
import com.github.tnoalex.foundation.language.Language
import com.github.tnoalex.issues.Severity
import com.github.tnoalex.issues.kotlin.withJava.IgnoredExceptionIssue
import com.github.tnoalex.processor.IssueProcessor
import com.github.tnoalex.processor.utils.filePath
import com.github.tnoalex.processor.utils.refCanNotResolveWarn
import com.github.tnoalex.processor.utils.startLine
import com.intellij.psi.*
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.analysis.api.KaExperimentalApi
import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.symbols.KaSymbolVisibility
import org.jetbrains.kotlin.analysis.api.types.KaType
import org.jetbrains.kotlin.asJava.elements.KtLightElement
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.types.Variance
import org.jetbrains.kotlin.utils.addToStdlib.ifTrue
import org.slf4j.LoggerFactory


@Component
@Suitable(LaunchEnvironment.CLI)
class IgnoredExceptionProcessor : IssueProcessor {
    override val severity: Severity = Severity.CODE_SMELL
    override val supportLanguage: List<Language> = listOf(JavaLanguage, KotlinLanguage)

    @EventListener(filterClazz = [KtFile::class, PsiJavaFile::class])
    override fun process(psiFile: PsiFile) {
        when (psiFile) {
            is KtFile -> psiFile.accept(ktApiVisitor)
            is PsiJavaFile -> psiFile.accept(javaCallExpressionVisitor)
        }
    }

    private val ktApiVisitor = object : KtTreeVisitorVoid() {
        override fun visitNamedFunction(function: KtNamedFunction) {
            analyze {
                function.symbol.let {
                    if (it.visibility != KaSymbolVisibility.PUBLIC) return@analyze
                    if (it.annotations.classIds.contains(THROWS_CLASS_ID)) return@analyze
                }
                function.accept(ThrowExpressionVisitor(false))
            }
            super.visitNamedFunction(function)
        }
    }

    private val javaCallExpressionVisitor = object : JavaRecursiveElementVisitor() {
        override fun visitCallExpression(callExpression: PsiCallExpression) {
            if (PsiTreeUtil.getParentOfType(
                    callExpression,
                    PsiTryStatement::class.java
                ) != null
            ) super.visitCallExpression(callExpression)
            val parent =
                PsiTreeUtil.getParentOfType(callExpression, PsiMethod::class.java) ?: return super.visitCallExpression(
                    callExpression
                )
            parent.throwsList.referencedTypes.isNotEmpty().ifTrue {
                return super.visitCallExpression(callExpression)
            }
            callExpression.accept(javaReferenceVisitor)
            super.visitCallExpression(callExpression)
        }
    }

    private val javaReferenceVisitor = object : JavaRecursiveElementVisitor() {
        override fun visitReferenceExpression(expression: PsiReferenceExpression) {
            expression.references.forEach {
                try {
                    val psiElement = it.resolve()
                    if (psiElement !is KtLightElement<*, *>) return@forEach
                    val ktOrigin = psiElement.kotlinOrigin ?: return@forEach
                    if (isAnnotatedWithThrows(ktOrigin)) return@forEach
                    ktOrigin.accept(ThrowExpressionVisitor(true))
                } catch (_: RuntimeException) {
                    logger.refCanNotResolveWarn(expression)
                    return@forEach
                }
            }
            super.visitReferenceExpression(expression)
        }
    }

    private fun isAnnotatedWithThrows(element: KtElement): Boolean {
        return analyze {
            val symbol = if (element is KtDeclaration) {
                element.symbol
            } else null
            symbol?.annotations?.classIds?.contains(THROWS_CLASS_ID) ?: false
        }
    }

    private inner class ThrowExpressionVisitor(
        private val calledByJava: Boolean
    ) : KtTreeVisitorVoid() {
        override fun visitThrowExpression(expression: KtThrowExpression) {
            val throws = expression.thrownExpression ?: return super.visitThrowExpression(expression)
            analyze {
                val exprType = throws.expressionType ?: let {
                    logger.refCanNotResolveWarn(expression)
                    return@analyze
                }
                reportIfIsCheckedException(expression, exprType, calledByJava)
            }
            super.visitThrowExpression(expression)
        }
    }

    @OptIn(KaExperimentalApi::class)
    private fun KaSession.reportIfIsCheckedException(element: KtExpression, type: KaType, calledByJava: Boolean) {
        if (type.isClassType(JAVA_RUNTIME_EXCEPTION_CLASS_ID)) return
        var anySuperIsRuntimeException = false
        for (superType in type.allSupertypes) {
            if (superType.isClassType(JAVA_ERROR_CLASS_ID)) return
            if (superType.isClassType(JAVA_RUNTIME_EXCEPTION_CLASS_ID)) {
                anySuperIsRuntimeException = true
                break
            }
        }
        if (!anySuperIsRuntimeException) {
            reportIssue(element, type.render(position = Variance.INVARIANT), calledByJava)
        }
    }

    private fun reportIssue(expression: KtExpression, exceptions: String, calledByJava: Boolean) {
        val issue = IgnoredExceptionIssue(
            expression.filePath,
            expression.text,
            exceptions,
            expression.startLine,
            calledByJava
        )
        if (issue !in context.issues) {
            context.reportIssue(issue)
        } else {
            (context.issues.find { it == issue } as? IgnoredExceptionIssue)?.let {
                if (calledByJava && !it.calledByJava) {
                    it.calledByJava = true
                }
            }
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(IgnoredExceptionProcessor::class.java)
        private val THROWS_CLASS_ID = ClassId(FqName("kotlin.jvm"), FqName("Throws"), false)
        private val JAVA_RUNTIME_EXCEPTION_CLASS_ID = ClassId(FqName("java.lang"), FqName("RuntimeException"), false)
        private val JAVA_ERROR_CLASS_ID = ClassId(FqName("java.lang"), FqName("Error"), false)
    }
}