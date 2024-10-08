package com.github.tnoalex.processor.kotlin.withJava

import com.github.tnoalex.foundation.ApplicationContext
import com.github.tnoalex.foundation.LaunchEnvironment
import com.github.tnoalex.foundation.bean.Component
import com.github.tnoalex.foundation.bean.Suitable
import com.github.tnoalex.foundation.eventbus.EventListener
import com.github.tnoalex.foundation.language.JavaLanguage
import com.github.tnoalex.foundation.language.KotlinLanguage
import com.github.tnoalex.foundation.language.Language
import com.github.tnoalex.issues.ConfidenceLevel
import com.github.tnoalex.issues.Severity
import com.github.tnoalex.issues.kotlin.withJava.*
import com.github.tnoalex.issues.kotlin.withJava.nonnullAssertion.NonNullAssertionOnNullableTypeIssue
import com.github.tnoalex.issues.kotlin.withJava.nonnullAssertion.NonNullAssertionOnPlatformTypeIssue
import com.github.tnoalex.processor.IssueProcessor
import com.github.tnoalex.processor.utils.*
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiModifierListOwner
import com.intellij.psi.impl.source.PsiClassReferenceType
import com.intellij.psi.impl.source.tree.LeafPsiElement
import org.jetbrains.kotlin.idea.references.mainReference
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.calls.smartcasts.DataFlowValueFactory
import org.jetbrains.kotlin.resolve.calls.smartcasts.Nullability
import org.jetbrains.kotlin.types.*
import org.jetbrains.kotlin.types.checker.SimpleClassicTypeSystemContext.isNullableType
import org.slf4j.LoggerFactory

@Component
@Suitable(LaunchEnvironment.CLI)
class UncertainNullablePlatformTypeProcessor : IssueProcessor {
    override val severity: Severity = Severity.CODE_SMELL
    override val supportLanguage: List<Language> = listOf(JavaLanguage, KotlinLanguage)
    val dataFlowValueFactory = ApplicationContext.getBeanOfType(DataFlowValueFactory::class.java).first()

    @EventListener(filterClazz = [KtFile::class])
    override fun process(psiFile: PsiFile) {
        psiFile.accept(kotlinPropertyVisitor)
    }

    private val kotlinPropertyVisitor = object : KtTreeVisitorVoid() {
        override fun visitCallExpression(expression: KtCallExpression) {
            if (context.confidenceLevel <= NullablePassedToPlatformParamIssue.normal) {
                checkParameter(expression)
            }
            super.visitCallExpression(expression)
        }

        override fun visitPostfixExpression(expression: KtPostfixExpression) {
            if (context.confidenceLevel <= NonNullAssertionOnPlatformTypeIssue.normal
                || context.confidenceLevel <= NonNullAssertionOnNullableTypeIssue.normal
            ) {
                checkNonNullAssertion(expression)
            }
            super.visitPostfixExpression(expression)
        }

        private fun checkNonNullAssertion(expression: KtPostfixExpression) {
            if (expression.operationToken != KtTokens.EXCLEXCL) return
            val baseExpr = expression.baseExpression ?: return
            val bindingContext = expression.bindingContext
            val type = bindingContext.getType(baseExpr) ?: return
            if (type.isDynamic()) return
            val nullability = getNullability(bindingContext, baseExpr, dataFlowValueFactory, type)
            if (nullability != Nullability.NOT_NULL) {
                if (context.confidenceLevel <= NonNullAssertionOnPlatformTypeIssue.normal
                    && type.isFlexibleRecursive()
                ) {
                    context.reportIssue(
                        NonNullAssertionOnPlatformTypeIssue(
                            expression.filePath,
                            expression.text.orEmpty(),
                            expression.startLine
                        )
                    )
                } else if (context.confidenceLevel <= NonNullAssertionOnNullableTypeIssue.normal
                    && type.isNullableType()
                ) {
                    context.reportIssue(
                        NonNullAssertionOnNullableTypeIssue(
                            expression.filePath,
                            expression.text.orEmpty(),
                            expression.startLine
                        )
                    )
                }
            }
        }

        override fun visitBinaryWithTypeRHSExpression(expression: KtBinaryExpressionWithTypeRHS) {
            if (context.confidenceLevel <= UncertainNullablePlatformExpressionUsageIssue.cast) {
                checkCast(expression)
            }
            super.visitBinaryWithTypeRHSExpression(expression)
        }

        private fun checkCast(expression: KtBinaryExpressionWithTypeRHS) {
            val expectedTypePsi = expression.right ?: return
            val elementType = expression.operationReference.getReferencedNameElementType()
            if (elementType != KtTokens.AS_KEYWORD) return
            val bindingContext = expression.bindingContext
            val expectedType = bindingContext[BindingContext.TYPE, expectedTypePsi] ?: return
            val leftExpr = expression.left
            val leftType = bindingContext.getType(leftExpr) ?: return
            val nullability = getNullability(bindingContext, leftExpr, dataFlowValueFactory, leftType)
            if (!expectedType.isMarkedNullable &&
                nullability != Nullability.NOT_NULL && leftType.isFlexibleRecursive()
            ) {
                context.reportIssue(
                    UncertainNullablePlatformExpressionUsageIssue(
                        expression.filePath,
                        expression.text ?: "",
                        expression.startLine,
                        expectedType.toString(),
                        leftType.toString(),
                        nullability?.toString() ?: "no smart cast",
                        ConfidenceLevel.LOW
                    )
                )
            }
        }

        override fun visitExpression(expression: KtExpression) {
            val bindingContext = expression.bindingContext
            if (context.confidenceLevel <= UncertainNullablePlatformExpressionUsageIssue.normal) {
                checkExpected(bindingContext, expression)
            }
            if (context.confidenceLevel <= UncertainNullablePlatformCallerIssue.normal) {
                checkCaller(bindingContext, expression)
            }
            super.visitExpression(expression)
        }

        private fun checkParameter(expression: KtCallExpression) {
            val bindingContext = expression.bindingContext
            val callee = expression.calleeExpression ?: return
            val calleeTarget = callee.mainReference?.resolve() ?: return
            if (calleeTarget is KtElement) return
            if (calleeTarget !is PsiMethod) return
            val args = expression.valueArguments
            val typeParams = calleeTarget.typeParameters
            if (calleeTarget.parameters.size != args.size) return
            for ((index, pair) in args.zip(calleeTarget.parameters).withIndex()) {
                val (actualArg, needArg) = pair
                val argumentExpression = actualArg.getArgumentExpression() ?: continue
                val actualType = bindingContext.getType(argumentExpression) ?: continue
                if (!actualType.isMarkedNullable) continue
                if (needArg !is PsiModifierListOwner) continue
                val isNotPlatformType = needArg.annotations.any {
                    val qn = (it.qualifiedName ?: return@any false).split(".")
                    qn.contains("NotNull") || qn.contains("Nullable")
                }
                if (isNotPlatformType) continue
                val needType = needArg.type
                if (needType is PsiClassReferenceType && needType.reference.resolve() in typeParams) continue
                context.reportIssue(
                    NullablePassedToPlatformParamIssue(
                        expression.filePath,
                        expression.text!!,
                        expression.startLine,
                        calleeTarget.filePath,
                        calleeTarget.name,
                        calleeTarget.startLine,
                        index
                    )
                )
            }
        }

        private fun checkCaller(bindingContext: BindingContext, expression: KtExpression) {
            val prevSibling = expression.prevSibling
            if (prevSibling !is LeafPsiElement) return
            if (prevSibling.elementType != KtTokens.DOT) return
            val callerExpr = prevSibling.prevSibling ?: return
            if (callerExpr !is KtExpression) return
            val callerType = bindingContext.getType(callerExpr) ?: return
            if (callerType.isDynamic()) return
            val nullability = getNullability(bindingContext, callerExpr, dataFlowValueFactory, callerType)
            if (nullability != Nullability.NOT_NULL && callerType.isFlexibleRecursive()) {
                context.reportIssue(
                    UncertainNullablePlatformCallerIssue(
                        callerExpr.filePath,
                        callerExpr.text!!,
                        callerExpr.startLine,
                        nullability?.toString() ?: "no smart cast",
                        callerExpr.text!!
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
                            expression.filePath,
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