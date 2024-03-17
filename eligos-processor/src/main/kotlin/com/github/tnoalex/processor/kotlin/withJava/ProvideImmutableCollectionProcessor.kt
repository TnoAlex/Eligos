package com.github.tnoalex.processor.kotlin.withJava

import com.github.tnoalex.foundation.LaunchEnvironment
import com.github.tnoalex.foundation.bean.Component
import com.github.tnoalex.foundation.bean.Suitable
import com.github.tnoalex.foundation.eventbus.EventListener
import com.github.tnoalex.issues.kotlin.withJava.ProvideImmutableCollectionIssue
import com.github.tnoalex.processor.PsiProcessor
import com.github.tnoalex.processor.utils.resolveToDescriptorIfAny
import com.github.tnoalex.processor.utils.startLine
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

    @EventListener
    fun process(javaFile: PsiJavaFile) {
        javaFile.accept(javaFileVisitorVoid)
    }

    private val javaFileVisitorVoid = object : JavaRecursiveElementVisitor() {
        override fun visitMethodCallExpression(expression: PsiMethodCallExpression) {
            val targetFunc = try {
                expression.methodExpression.resolve() ?: let {
                    logger.warn("Can not resolve call expression in file ${expression.containingFile.name} at line ${expression.startLine}")
                    return
                }
            } catch (e: IllegalArgumentException) {
                logger.warn("Can not resolve reference in file ${expression.containingFile.virtualFile.path},line ${expression.startLine}")
            }
            if (targetFunc !is KtLightElement<*, *>) return
            val ktOrigin = targetFunc.kotlinOrigin ?: let {// maybe kotlin enum
                logger.warn(
                    "Can not resolve origin kotlin file in expression ${expression.text} " +
                            "at file ${expression.containingFile.virtualFile.path},line ${expression.startLine}"
                )
                return
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
                logger.warn("Unknown return type of element in ${ktOrigin.containingFile.name} at line ${ktOrigin.startLine}")
                return
            }
            if (returnType.getKotlinTypeFqName(false) !in KOTLIN_IMMUTABLE_FQNAME) return
            val className = PsiTreeUtil.getParentOfType(expression, PsiClass::class.java)?.qualifiedName
                ?: throw RuntimeException("Can not find parent class of expression ${expression.text}")
            context.reportIssue(
                ProvideImmutableCollectionIssue(
                    hashSetOf(expression.containingFile.virtualFile.path, ktOrigin.containingFile.virtualFile.path),
                    (ktOrigin as KtCallableDeclaration).fqName?.asString() ?: let {
                        logger.warn("Unknown function name in file ${ktOrigin.containingFile.name} at line ${ktOrigin.startLine}")
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

        private val KOTLIN_IMMUTABLE_FQNAME =
            listOf("kotlin.collections.List", "kotlin.collections.Set", "kotlin.collections.Map")
    }
}