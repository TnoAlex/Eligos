package com.github.tnoalex.processor.kotlin.withJava

import com.github.tnoalex.foundation.LaunchEnvironment
import com.github.tnoalex.foundation.bean.Component
import com.github.tnoalex.foundation.bean.Suitable
import com.github.tnoalex.foundation.eventbus.EventListener
import com.github.tnoalex.issues.kotlin.withJava.IgnoreExceptionIssue
import com.github.tnoalex.processor.PsiProcessor
import com.github.tnoalex.processor.utils.resolveToDescriptorIfAny
import com.github.tnoalex.processor.utils.startLine
import com.github.tnoalex.processor.utils.superTypes
import com.intellij.psi.*
import com.intellij.psi.impl.compiled.ClsClassImpl
import com.intellij.psi.impl.compiled.ClsMethodImpl
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.PsiTypesUtil
import org.jetbrains.kotlin.asJava.elements.KtLightElement
import org.jetbrains.kotlin.js.descriptorUtils.getKotlinTypeFqName
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.referenceExpression
import org.jetbrains.kotlin.resolve.descriptorUtil.isPublishedApi
import org.jetbrains.kotlin.utils.addToStdlib.ifTrue
import org.slf4j.LoggerFactory

@Component
@Suitable(LaunchEnvironment.CLI)
class IgnoreExceptionProcessor : PsiProcessor {
    override val supportLanguage: List<String>
        get() = listOf("kotlin", "java")

    @EventListener
    fun process(psiFile: PsiFile) {
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
                if (!it.isPublishedApi()) return
                if (it.annotations.findAnnotation(THROWS_FQ_NAME) != null) return
            } ?:return
            function.accept(ktThrowVisitor)
            super.visitNamedFunction(function)
        }
    }

    private val javaCallExpressionVisitor = object : JavaRecursiveElementVisitor() {
        override fun visitCallExpression(callExpression: PsiCallExpression) {
            if (PsiTreeUtil.getParentOfType(callExpression, PsiTryStatement::class.java) != null) return
            val parent = PsiTreeUtil.getParentOfType(callExpression, PsiMethod::class.java) ?: return
            parent.throwsList.referencedTypes.isNotEmpty().ifTrue { return }
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
                    ktOrigin.accept(ktThrowVisitor)
                } catch (e: RuntimeException) {
                    logger.warn(
                        "Can not resolve reference in file ${expression.containingFile.virtualFile.path}," +
                                "line ${expression.startLine}"
                    )
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

    private val ktThrowVisitor = object : KtTreeVisitorVoid() {
        override fun visitThrowExpression(expression: KtThrowExpression) {
            val throws = expression.thrownExpression ?: return
            val exceptions = throws.referenceExpression()?.let {
                it.references.mapNotNull { r ->
                    try {
                        r.resolve()
                    } catch (e: RuntimeException) {
                        logger.warn(
                            "Can not resolve expression in file ${expression.containingFile.virtualFile.path}" +
                                    ",line ${expression.startLine}"
                        )
                    }
                }
            } ?: return
            val runtimeException =
                PsiType.getTypeByName(JAVA_RUNTIME_EXCEPTION_FQ_NAME, expression.project, expression.resolveScope)
            findCheckedException(throws, exceptions, runtimeException)
            super.visitThrowExpression(expression)
        }
    }

    private fun findCheckedException(element: KtExpression, exceptions: List<Any>, runtimeException: PsiType) {
        exceptions.forEach {
            when (it) {
                is ClsMethodImpl -> {
                    val clazz = PsiTreeUtil.getParentOfType(it, ClsClassImpl::class.java) ?: return@forEach
                    val classType = PsiTypesUtil.getClassType(clazz)
                    classType.isConvertibleFrom(runtimeException).ifTrue { return@forEach }
                    reportIssue(element, clazz.qualifiedName ?: "Unknown exception name")
                    return
                }

                is KtClass -> {
                    val superTypes = it.superTypes ?: return@forEach
                    superTypes.any { t -> t.getKotlinTypeFqName(false) == JAVA_RUNTIME_EXCEPTION_FQ_NAME }
                        .ifTrue { return@forEach }
                    reportIssue(element, it.fqName?.asString() ?: "Unknown exception name")
                    return
                }

                is KtConstructor<*> -> {
                    val clazz = PsiTreeUtil.getParentOfType(it, KtClass::class.java) ?: return@forEach
                    val superTypes = clazz.superTypes ?: return@forEach
                    superTypes.any { t -> t.getKotlinTypeFqName(false) == JAVA_RUNTIME_EXCEPTION_FQ_NAME }
                        .ifTrue { return@forEach }
                    reportIssue(element, clazz.fqName?.asString() ?: "Unknown exception name")
                    return
                }

                is KtTypeAlias -> {
                    val typeReference = it.getTypeReference() ?: return@forEach
                    if (typeReference.name == JAVA_RUNTIME_EXCEPTION_FQ_NAME) {
                        return@forEach
                    }
                    reportIssue(element, typeReference.name ?: "Unknown exception name")
                    return
                }
            }
        }
    }

    private fun reportIssue(expression: KtExpression, exceptions: String) {
        context.reportIssue(
            IgnoreExceptionIssue(
                expression.containingFile.virtualFile.path,
                expression.text,
                exceptions,
                expression.startLine
            )
        )
    }

    companion object {
        private val logger = LoggerFactory.getLogger(IgnoreExceptionProcessor::class.java)
        private val THROWS_FQ_NAME = FqName("kotlin.jvm.Throws")
        private const val JAVA_RUNTIME_EXCEPTION_FQ_NAME = "java.lang.RuntimeException"
    }
}