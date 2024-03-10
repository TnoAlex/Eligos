package com.github.tnoalex.processor.kotlin.withJava

import com.github.tnoalex.foundation.LaunchEnvironment
import com.github.tnoalex.foundation.bean.Component
import com.github.tnoalex.foundation.bean.Suitable
import com.github.tnoalex.foundation.eventbus.EventListener
import com.github.tnoalex.issues.ProvideImmutableCollectionIssue
import com.github.tnoalex.processor.PsiProcessor
import com.github.tnoalex.processor.utils.resolveToDescriptorIfAny
import com.github.tnoalex.processor.utils.startLine
import com.intellij.psi.JavaRecursiveElementVisitor
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.PsiMethodCallExpression
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.asJava.elements.KtLightElement
import org.jetbrains.kotlin.js.descriptorUtils.getKotlinTypeFqName
import org.jetbrains.kotlin.psi.KtNamedFunction
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
            val targetFunc = expression.methodExpression.resolve() ?: let {
                logger.warn("Can not resolve call expression in file ${expression.containingFile.name} at line ${expression.startLine}")
                return
            }
            if (targetFunc !is KtLightElement<*, *>) return
            val ktOrigin = targetFunc.kotlinOrigin ?: throw RuntimeException("Can not resolve origin kotlin file")
            require(ktOrigin is KtNamedFunction) { "Java method call target is not function" }
            val returnType = ktOrigin.resolveToDescriptorIfAny()?.returnType ?: let {
                logger.warn("Unknown return type of function in ${ktOrigin.containingFile.name} at line ${ktOrigin.startLine}")
                return
            }
            if (returnType.getKotlinTypeFqName(false) !in KOTLIN_IMMUTABLE_FQNAME) return
            val className = PsiTreeUtil.getParentOfType(expression, PsiClass::class.java)?.qualifiedName
                ?: throw RuntimeException("Can not find parent class of expression ${expression.text}")
            context.reportIssue(
                ProvideImmutableCollectionIssue(
                    hashSetOf(expression.containingFile.virtualFile.path, ktOrigin.containingFile.virtualFile.path),
                    ktOrigin.fqName?.asString() ?: let {
                        logger.warn("Unknown function name in file ${ktOrigin.containingFile.name} at line ${ktOrigin.startLine}")
                        "unknown func name"
                    },
                    expression.startLine,
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