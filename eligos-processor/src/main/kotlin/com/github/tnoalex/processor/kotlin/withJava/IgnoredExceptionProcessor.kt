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
import com.github.tnoalex.processor.utils.*
import com.intellij.psi.*
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.asJava.elements.KtLightElement
import org.jetbrains.kotlin.js.descriptorUtils.getKotlinTypeFqName
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.resolve.descriptorUtil.isEffectivelyPublicApi
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.typeUtil.supertypes
import org.jetbrains.kotlin.utils.addToStdlib.ifFalse
import org.jetbrains.kotlin.utils.addToStdlib.ifTrue
import org.slf4j.LoggerFactory


@Component
@Suitable(LaunchEnvironment.CLI)
class IgnoredExceptionProcessor : IssueProcessor {
    override val severity: Severity
        get() = Severity.CODE_SMELL
    override val supportLanguage: List<Language>
        get() = listOf(JavaLanguage, KotlinLanguage)

    @EventListener(filterClazz = [KtFile::class,PsiJavaFile::class])
    override fun process(psiFile: PsiFile) {
        when (psiFile) {
            is KtFile -> {
                psiFile.accept(ktApiVisitor)
            }

            is PsiJavaFile -> {
                psiFile.accept(javaCallExpressionVisitor)
            }
        }
    }

    private val ktApiVisitor = object : KtTreeVisitorVoid() {
        override fun visitNamedFunction(function: KtNamedFunction) {
            function.resolveToDescriptorIfAny()?.let {
                if (!it.isEffectivelyPublicApi) return super.visitNamedFunction(function)
                if (it.annotations.findAnnotation(THROWS_FQ_NAME) != null) return super.visitNamedFunction(function)
            } ?: super.visitNamedFunction(function)
            function.accept(ThrowExpressionVisitor(false))
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
                } catch (e: RuntimeException) {
                    logger.refCanNotResolveWarn(expression)
                    return@forEach
                }
            }
            super.visitReferenceExpression(expression)
        }
    }

    private fun isAnnotatedWithThrows(element: KtElement): Boolean {
        val declaration = when (element) {
            is KtConstructor<*> -> {
                element.resolveToDescriptorIfAny()
            }

            is KtNamedFunction -> {
                element.resolveToDescriptorIfAny()
            }

            is KtProperty -> {
                element.resolveToDescriptorIfAny()
            }

            else -> null
        }
        if (declaration == null) return false
        if (declaration.annotations.findAnnotation(THROWS_FQ_NAME) != null) return true
        return false
    }

    private inner class ThrowExpressionVisitor(
        private val calledByJava: Boolean
    ) : KtTreeVisitorVoid() {
        override fun visitThrowExpression(expression: KtThrowExpression) {
            val throws = expression.thrownExpression ?: return super.visitThrowExpression(expression)
            val exceptions = expression.bindingContext.getType(throws) ?: let {
                logger.refCanNotResolveWarn(expression)
                return super.visitThrowExpression(expression)
            }
            findCheckedException(expression, exceptions, calledByJava)
            super.visitThrowExpression(expression)
        }
    }

    private fun findCheckedException(element: KtExpression, exception: KotlinType, calledByJava: Boolean) {
        if (exception.getKotlinTypeFqName(false) == JAVA_RUNTIME_EXCEPTION_FQ_NAME) return
        exception.supertypes().any { it.getKotlinTypeFqName(false) == JAVA_ERROR }.ifTrue {
            return
        }
        exception.supertypes().any {
            it.getKotlinTypeFqName(false) == JAVA_RUNTIME_EXCEPTION_FQ_NAME
        }.ifFalse {
            reportIssue(element, exception.getKotlinTypeFqName(false), calledByJava)
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
                if (calledByJava && !it.calledByJava){
                    it.calledByJava = true
                }
            }
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(IgnoredExceptionProcessor::class.java)
        private val THROWS_FQ_NAME = FqName("kotlin.jvm.Throws")
        private const val JAVA_RUNTIME_EXCEPTION_FQ_NAME = "java.lang.RuntimeException"
        private const val JAVA_ERROR = "java.lang.Error"
    }
}