package com.github.tnoalex.processor.kotlin.withJava

import com.github.tnoalex.foundation.ApplicationContext
import com.github.tnoalex.foundation.LaunchEnvironment
import com.github.tnoalex.foundation.bean.Component
import com.github.tnoalex.foundation.bean.Suitable
import com.github.tnoalex.foundation.eventbus.EventListener
import com.github.tnoalex.foundation.language.JavaLanguage
import com.github.tnoalex.foundation.language.KotlinLanguage
import com.github.tnoalex.foundation.language.Language
import com.github.tnoalex.issues.Severity
import com.github.tnoalex.issues.kotlin.withJava.UncertainNullablePlatformCallerIssue
import com.github.tnoalex.issues.kotlin.withJava.UncertainNullablePlatformExpressionUsageIssue
import com.github.tnoalex.issues.kotlin.withJava.UncertainNullablePlatformTypeInPropertyIssue
import com.github.tnoalex.processor.IssueProcessor
import com.github.tnoalex.processor.utils.*
import com.intellij.psi.PsiFile
import com.intellij.psi.impl.source.tree.LeafPsiElement
import org.jetbrains.kotlin.idea.references.mainReference
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtTreeVisitorVoid
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.calls.smartcasts.DataFlowValueFactory
import org.jetbrains.kotlin.resolve.calls.smartcasts.Nullability
import org.jetbrains.kotlin.types.*
import org.slf4j.LoggerFactory

@Component
@Suitable(LaunchEnvironment.CLI)
class UncertainNullablePlatformTypeProcessor : IssueProcessor {
    override val severity: Severity
        get() = Severity.CODE_SMELL
    override val supportLanguage: List<Language>
        get() = listOf(JavaLanguage, KotlinLanguage)
    val dataFlowValueFactory = ApplicationContext.getBeanOfType(DataFlowValueFactory::class.java).first()

    @EventListener(filterClazz = [KtFile::class])
    override fun process(psiFile: PsiFile) {
        psiFile.accept(kotlinPropertyVisitor)
    }

    private val kotlinPropertyVisitor = object : KtTreeVisitorVoid() {
        override fun visitExpression(expression: KtExpression) {
            val bindingContext = expression.bindingContext
            checkExpected(bindingContext, expression)
            checkCaller(bindingContext, expression)
            super.visitExpression(expression)
        }

        private fun checkCaller(bindingContext: BindingContext, expression: KtExpression) {
            val prevSibling = expression.prevSibling
            if (prevSibling !is LeafPsiElement) return
            if (prevSibling.elementType != KtTokens.DOT) return
            val callerExpr = prevSibling.prevSibling ?: return
            if (callerExpr !is KtExpression) return
            val callerType = bindingContext.getType(callerExpr) ?: return
            if (callerType.isDynamic()) return
            val nullability = getNullability(bindingContext, expression, dataFlowValueFactory, callerType)
            if (nullability != Nullability.NOT_NULL && callerType.isFlexibleRecursive()) {
                context.reportIssue(
                    UncertainNullablePlatformCallerIssue(
                        callerExpr.containingFile.virtualFile.path,
                        callerExpr.text!!,
                        callerExpr.startLine,
                        nullability?.toString() ?: "no smart cast"
                    )
                )
            }
        }

        private fun checkExpected(bindingContext: BindingContext, expression: KtExpression) {
            val expectedType = bindingContext[BindingContext.EXPECTED_EXPRESSION_TYPE, expression]
                ?: return
            if (expectedType.isNullable() || expectedType.isFlexibleRecursive()) {
                return
            }
            val type = bindingContext.getType(expression) ?: return
            if (type.isDynamic()) return
            val nullability = getNullability(bindingContext, expression, dataFlowValueFactory, type)
            if (nullability != Nullability.NOT_NULL && type.isFlexibleRecursive()) {
                val target = expression.mainReference?.resolve()
                if (target !is KtProperty) {
                    context.reportIssue(
                        UncertainNullablePlatformExpressionUsageIssue(
                            expression.containingFile.virtualFile.path,
                            expression.parent.text!!,
                            expression.startLine,
                            expectedType.toString(),
                            type.toString(),
                            nullability?.toString() ?: "no smart cast"
                        )
                    )
                } else {
                    reportProperty(target)
                }
            }
        }


        override fun visitProperty(property: KtProperty) {
            if (property.isLocal) return super.visitProperty(property)
            reportProperty(property)
            super.visitProperty(property)
        }

        private fun reportProperty(property: KtProperty) {
            val descriptor = property.resolveToDescriptorIfAny() ?: let {
                logger.typeCanNotResolveWarn("property", property)
                return
            }
            val propertyType = descriptor.type
            // dynamic type can not be resolved
            if (propertyType.isDynamic()) return
            if (propertyType.isFlexibleRecursive()) {
                //found platform type
                context.reportIssue(
                    UncertainNullablePlatformTypeInPropertyIssue(
                        property.filePath,
                        property.text,
                        property.name ?: let {
                            logger.nameCanNotResolveWarn("property", property)
                            "unknown property name"
                        },
                        property.startLine,
                        propertyType.upperIfFlexible().toString(),
                        propertyType.lowerIfFlexible().toString(),
                        property.isTopLevel,
                        property.isLocal
                    )
                )
            }
            return
        }
    }

    companion object {
        @JvmStatic
        private val logger = LoggerFactory.getLogger(UncertainNullablePlatformTypeProcessor::class.java)
    }

    private fun KotlinType.isFlexibleRecursive(): Boolean {
        if (isFlexible()) return true
        return arguments.any { !it.isStarProjection && it.type.isFlexibleRecursive() }
    }
}