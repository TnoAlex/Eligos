package com.github.tnoalex.processor.kotlin.withJava

import com.github.tnoalex.foundation.LaunchEnvironment
import com.github.tnoalex.foundation.bean.Component
import com.github.tnoalex.foundation.bean.Suitable
import com.github.tnoalex.foundation.eventbus.EventListener
import com.github.tnoalex.issues.kotlin.withJava.ProvideImmutableCollectionIssue
import com.github.tnoalex.processor.PsiProcessor
import com.github.tnoalex.processor.utils.*
import com.intellij.psi.JavaRecursiveElementVisitor
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.PsiMethodCallExpression
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.asJava.elements.KtLightElement
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.js.descriptorUtils.getKotlinTypeFqName
import org.jetbrains.kotlin.psi.KtCallableDeclaration
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtParameter
import org.jetbrains.kotlin.types.KotlinType
import org.slf4j.LoggerFactory

@Component
@Suitable(LaunchEnvironment.CLI)
class ProvideImmutableCollectionProcessor : PsiProcessor {
    override val supportLanguage: List<String>
        get() = listOf("java", "kotlin")

    @EventListener
    fun process(javaFile: PsiJavaFile) {
        javaFile.accept(javaFileVisitorVoid)
    }

    private val javaFileVisitorVoid = object : JavaRecursiveElementVisitor() {
        override fun visitMethodCallExpression(expression: PsiMethodCallExpression) {
            val targetFunc = try {
                expression.methodExpression.resolve() ?: let {
                    logger.refCanNotResolveWarn(expression)
                    return super.visitMethodCallExpression(expression)
                }
            } catch (e: RuntimeException) {
                logger.refCanNotResolveWarn(expression)
            }
            if (targetFunc !is KtLightElement<*, *>) return super.visitMethodCallExpression(expression)
            val ktOrigin = targetFunc.kotlinOrigin ?: let {// maybe kotlin enum
                logger.kotlinOriginCanNotResolveWarn("expression", expression)
                return super.visitMethodCallExpression(expression)
            }
            val returnType: KotlinType? = when (ktOrigin) {
                is KtNamedFunction -> {
                    ktOrigin.resolveToDescriptorIfAny()?.returnType
                }

                is KtParameter -> {
                    (ktOrigin.resolveToDescriptorIfAny() as PropertyDescriptor).returnType
                }

                else -> {
                    null
                }
            }
            if (returnType == null) {
                logger.nameCanNotResolveWarn("return type", expression)
                return super.visitMethodCallExpression(expression)
            }
            if (returnType.getKotlinTypeFqName(false) !in KOTLIN_IMMUTABLE_FQ_NAME)
                return super.visitMethodCallExpression(expression)
            val className = PsiTreeUtil.getParentOfType(expression, PsiClass::class.java)?.qualifiedName
                ?: "AnonymousInnerClass"
            context.reportIssue(
                ProvideImmutableCollectionIssue(
                    hashSetOf(expression.filePath, ktOrigin.filePath),
                    (ktOrigin as KtCallableDeclaration).fqName?.asString() ?: let {
                        logger.nameCanNotResolveWarn("function", ktOrigin)
                        "unknown func name"
                    },
                    ktOrigin is KtNamedFunction,
                    ktOrigin is KtParameter,
                    expression.startLine,
                    expression.text,
                    className
                )
            )
            super.visitMethodCallExpression(expression)
        }
    }

    companion object {
        @JvmStatic
        private val logger = LoggerFactory.getLogger(ProvideImmutableCollectionProcessor::class.java)

        private val KOTLIN_IMMUTABLE_FQ_NAME =
            listOf("kotlin.collections.List", "kotlin.collections.Set", "kotlin.collections.Map")
    }
}