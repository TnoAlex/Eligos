package com.github.tnoalex.processor.kotlin.withJava

import com.github.tnoalex.foundation.LaunchEnvironment
import com.github.tnoalex.foundation.bean.Component
import com.github.tnoalex.foundation.bean.Suitable
import com.github.tnoalex.foundation.eventbus.EventListener
import com.github.tnoalex.foundation.language.JavaLanguage
import com.github.tnoalex.foundation.language.KotlinLanguage
import com.github.tnoalex.foundation.language.Language
import com.github.tnoalex.issues.Severity
import com.github.tnoalex.issues.kotlin.withJava.ProvideImmutableCollectionIssue
import com.github.tnoalex.processor.IssueProcessor
import com.github.tnoalex.processor.utils.*
import com.intellij.psi.*
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.analysis.api.symbols.KaCallableSymbol
import org.jetbrains.kotlin.analysis.api.types.symbol
import org.jetbrains.kotlin.asJava.elements.KtLightElement
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.psi.KtCallableDeclaration
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtParameter
import org.slf4j.LoggerFactory

@Component
@Suitable(LaunchEnvironment.CLI)
class ProvideImmutableCollectionProcessor : IssueProcessor {
    override val severity: Severity = Severity.CODE_SMELL
    override val supportLanguage: List<Language> = listOf(JavaLanguage, KotlinLanguage)

    @EventListener(filterClazz = [PsiJavaFile::class])
    override fun process(psiFile: PsiFile) {
        if (context.confidenceLevel > ProvideImmutableCollectionIssue.normal) {
            return
        }
        psiFile.accept(javaFileVisitorVoid)
    }

    private val javaFileVisitorVoid = object : JavaRecursiveElementVisitor() {
        override fun visitMethodCallExpression(expression: PsiMethodCallExpression) {
            val targetFunc = try {
                expression.methodExpression.resolve() ?: let {
                    logger.refCanNotResolveWarn(expression)
                    return super.visitMethodCallExpression(expression)
                }
            } catch (_: RuntimeException) {
                logger.refCanNotResolveWarn(expression)
            }
            if (targetFunc !is KtLightElement<*, *>) return super.visitMethodCallExpression(expression)
            val ktOrigin = targetFunc.kotlinOrigin ?: let {// maybe kotlin enum
                logger.kotlinOriginCanNotResolveWarn("expression", expression)
                return super.visitMethodCallExpression(expression)
            }
            analyze {
                ktOrigin as? KtDeclaration ?: return@analyze
                val returnType = when (val symbol = ktOrigin.symbol) {
                    is KaCallableSymbol -> symbol.returnType
                    else -> null
                }

                if (returnType == null) {
                    logger.nameCanNotResolveWarn("return type", expression)
                    return@analyze
                }
                if (returnType.symbol?.classId !in KOTLIN_IMMUTABLE_CLASS_IDS)
                    return@analyze
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
            }
            super.visitMethodCallExpression(expression)
        }
    }

    companion object {
        @JvmStatic
        private val logger = LoggerFactory.getLogger(ProvideImmutableCollectionProcessor::class.java)

        private val KOTLIN_IMMUTABLE_CLASS_IDS =
            listOf("kotlin/collections/List", "kotlin/collections/Set", "kotlin/collections/Map")
                .map { ClassId.fromString(it) }
    }
}