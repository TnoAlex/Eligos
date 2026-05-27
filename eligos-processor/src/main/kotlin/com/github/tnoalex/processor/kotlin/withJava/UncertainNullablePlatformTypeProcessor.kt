package com.github.tnoalex.processor.kotlin.withJava

import com.github.tnoalex.foundation.LaunchEnvironment
import com.github.tnoalex.foundation.bean.Component
import com.github.tnoalex.foundation.bean.Suitable
import com.github.tnoalex.foundation.eventbus.EventListener
import com.github.tnoalex.foundation.language.JavaLanguage
import com.github.tnoalex.foundation.language.KotlinLanguage
import com.github.tnoalex.foundation.language.Language
import com.github.tnoalex.issues.ConfidenceLevel
import com.github.tnoalex.issues.Severity
import com.github.tnoalex.issues.kotlin.withJava.NullablePassedToPlatformParamIssue
import com.github.tnoalex.issues.kotlin.withJava.UncertainNullablePlatformCallerIssue
import com.github.tnoalex.issues.kotlin.withJava.UncertainNullablePlatformExpressionUsageIssue
import com.github.tnoalex.issues.kotlin.withJava.UncertainNullablePlatformTypeInPropertyIssue
import com.github.tnoalex.issues.kotlin.withJava.nonnullAssertion.NonNullAssertionOnNullableTypeIssue
import com.github.tnoalex.issues.kotlin.withJava.nonnullAssertion.NonNullAssertionOnPlatformTypeIssue
import com.github.tnoalex.processor.IssueProcessor
import com.github.tnoalex.processor.utils.filePath
import com.github.tnoalex.processor.utils.nameCanNotResolveWarn
import com.github.tnoalex.processor.utils.resolveToDescriptorIfAny
import com.github.tnoalex.processor.utils.startLine
import com.github.tnoalex.processor.utils.typeCanNotResolveWarn
import com.intellij.psi.PsiFile
import com.intellij.psi.impl.source.tree.LeafPsiElement
import org.jetbrains.kotlin.analysis.api.KaExperimentalApi
import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.resolution.KaCompoundArrayAccessCall
import org.jetbrains.kotlin.analysis.api.resolution.KaCompoundVariableAccessCall
import org.jetbrains.kotlin.analysis.api.resolution.KaFunctionCall
import org.jetbrains.kotlin.analysis.api.resolution.KaSuccessCallInfo
import org.jetbrains.kotlin.analysis.api.resolution.KaVariableAccessCall
import org.jetbrains.kotlin.analysis.api.resolution.symbol
import org.jetbrains.kotlin.analysis.api.symbols.KaPropertySymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaVariableSymbol
import org.jetbrains.kotlin.analysis.api.symbols.isLocal
import org.jetbrains.kotlin.analysis.api.symbols.isTopLevel
import org.jetbrains.kotlin.analysis.api.symbols.name
import org.jetbrains.kotlin.analysis.api.symbols.symbol
import org.jetbrains.kotlin.analysis.api.types.KaClassType
import org.jetbrains.kotlin.analysis.api.types.KaType
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.parents
import org.jetbrains.kotlin.types.Variance
import org.jetbrains.kotlin.types.isDynamic
import org.slf4j.LoggerFactory

@Component
@Suitable(LaunchEnvironment.CLI)
class UncertainNullablePlatformTypeProcessor : IssueProcessor {
    override val severity: Severity = Severity.CODE_SMELL
    override val supportLanguage: List<Language> = listOf(JavaLanguage, KotlinLanguage)

    @EventListener(filterClazz = [KtFile::class])
    override fun process(psiFile: PsiFile) {
        psiFile.accept(kotlinPropertyVisitor)
    }

    @OptIn(KaExperimentalApi::class)
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
            analyze {
                // in a.b!! a.b is the base expression
                val base = expression.baseExpression ?: return@analyze
                val type = base.expressionType ?: return@analyze
                if (type.hasFlexibleNullability || type.isNullable) {
                    if (context.confidenceLevel <= NonNullAssertionOnPlatformTypeIssue.normal && type.isFlexibleRecursive()) {
                        context.reportIssue(
                            NonNullAssertionOnPlatformTypeIssue(
                                expression.filePath,
                                expression.text.orEmpty(),
                                expression.startLine
                            )
                        )
                    } else if (context.confidenceLevel <= NonNullAssertionOnNullableTypeIssue.normal
                        && type.isNullable
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
        }

        override fun visitBinaryWithTypeRHSExpression(expression: KtBinaryExpressionWithTypeRHS) {
            if (context.confidenceLevel <= UncertainNullablePlatformExpressionUsageIssue.cast) {
                checkCast(expression)
            }
            super.visitBinaryWithTypeRHSExpression(expression)
        }

        private fun checkCast(expression: KtBinaryExpressionWithTypeRHS) {
            val elementType = expression.operationReference.getReferencedNameElementType()
            if (elementType != KtTokens.AS_KEYWORD) return
            analyze {
                val expectedType = expression.expectedType ?: return@analyze
                val leftExpr = expression.left
                val leftType = leftExpr.expressionType ?: return@analyze
                if (!leftType.isMarkedNullable && expectedType.isNullable) {
                    context.reportIssue(
                        UncertainNullablePlatformExpressionUsageIssue(
                            expression.filePath,
                            expression.text ?: "",
                            expression.startLine,
                            expectedType.toString(),
                            leftType.toString(),
                            expectedType.render(position = Variance.INVARIANT),
                            ConfidenceLevel.LOW
                        )
                    )
                }
            }
        }

        override fun visitExpression(expression: KtExpression) {
            if (context.confidenceLevel <= UncertainNullablePlatformExpressionUsageIssue.normal) {
                checkExpected(expression)
            }
            if (context.confidenceLevel <= UncertainNullablePlatformCallerIssue.normal) {
                checkCaller(expression)
            }
            super.visitExpression(expression)
        }

        private fun checkParameter(expression: KtCallExpression) {
            expression.calleeExpression ?: return
            analyze {
                val calleeTarget = expression.resolveSymbol() ?: return@analyze
                val args = expression.valueArguments
                val valueParameters = calleeTarget.valueParameters
                if (valueParameters.size != args.size) return@analyze
                for ((index, pair) in args.zip(valueParameters).withIndex()) {
                    val (actualArg, needArg) = pair
                    val argumentExpression = actualArg.getArgumentExpression() ?: continue
                    val actualType = argumentExpression.expressionType ?: continue
                    if (!actualType.isMarkedNullable) continue
                    val isNotPlatformType = needArg.annotations.classIds.any {
                        val qn = it.asFqNameString()
                        qn.contains("NotNull") || qn.contains("Nullable")
                    }
                    if (isNotPlatformType) continue
                    val needType = needArg.returnType
                    if (needType.hasFlexibleNullability) {
                        val targetPsi = calleeTarget.psi
                        context.reportIssue(
                            NullablePassedToPlatformParamIssue(
                                expression.filePath,
                                expression.text!!,
                                expression.startLine,
                                targetPsi?.containingFile?.filePath ?: "unknown file",
                                calleeTarget.name?.toString() ?: "unknown method",
                                targetPsi?.startLine ?: -1,
                                index
                            )
                        )
                    }
                }
            }
        }

        private fun checkCaller(expression: KtExpression) {
            val prevSibling = expression.prevSibling
            if (prevSibling !is LeafPsiElement) return
            if (prevSibling.elementType != KtTokens.DOT) return
            val callerExpr = prevSibling.prevSibling ?: return
            if (callerExpr !is KtExpression) return
            analyze {
                val callerType = callerExpr.expressionType ?: return@analyze
                if (callerType.isNullable && callerType.isFlexibleRecursive()) {
                    context.reportIssue(
                        UncertainNullablePlatformCallerIssue(
                            callerExpr.filePath,
                            callerExpr.text!!,
                            callerExpr.startLine,
                            callerExpr.smartCastInfo?.smartCastType?.render(position = Variance.INVARIANT)
                                ?: "no smart cast",
                            callerExpr.text!!
                        )
                    )
                }
            }
        }

        private fun checkExpected(expression: KtExpression) {
            analyze {
                val expectedType = expression.expectedType ?: return@analyze
                if (expectedType.isNullable || expectedType.isFlexibleRecursive()) {
                    return@analyze
                }
                val type = expression.expressionType ?: return@analyze
                if ((type.hasFlexibleNullability || type.isNullable) && type.isFlexibleRecursive()) {
                    val callInfo = expression.resolveToCall()
                    if (callInfo !is KaSuccessCallInfo) return@analyze
                    when (val call = callInfo.call) {
                        is KaFunctionCall<*> -> {
                            val content = if (expression.parent.expectedType != null) {
                                expression.parent.text!!
                            } else {
                                expression.text
                            }
                            context.reportIssue(
                                UncertainNullablePlatformExpressionUsageIssue(
                                    expression.filePath,
                                    content,
                                    expression.startLine,
                                    expectedType.toString(),
                                    type.toString(),
                                    expression.smartCastInfo?.smartCastType?.render(position = Variance.INVARIANT)
                                        ?: "no smart cast"
                                )
                            )
                        }

                        is KaVariableAccessCall -> {
                            tryReportProperty(call.symbol)
                        }

                        else -> {}
                    }
                }
            }
        }


        override fun visitProperty(property: KtProperty) {
            if (property.isLocal) return super.visitProperty(property)
            tryReportProperty(property)
            super.visitProperty(property)
        }

        private fun KaSession.tryReportProperty(property: KaVariableSymbol) {
            val propertyType = property.returnType
            if (propertyType.isFlexibleRecursive()) {
                val psi = property.psi
                //found platform type
                context.reportIssue(
                    UncertainNullablePlatformTypeInPropertyIssue(
                        psi?.filePath ?: "unknown file",
                        psi?.text ?: "unknown text",
                        property.name.toString(),
                        psi?.startLine ?: -1,
                        propertyType.upperBoundIfFlexible().toString(),
                        propertyType.lowerBoundIfFlexible().toString(),
                        property.isTopLevel,
                        property.isLocal
                    )
                )
            }
            return
        }

        private fun tryReportProperty(property: KtProperty) {
            analyze { tryReportProperty(property.symbol) }
        }
    }

    companion object {
        @JvmStatic
        private val logger = LoggerFactory.getLogger(UncertainNullablePlatformTypeProcessor::class.java)
    }

    context(session: KaSession)
    private fun KaType.isFlexibleRecursive(): Boolean {
        with(session) {
            if (hasFlexibleNullability) return true
            if (this is KaClassType) {
                return typeArguments.any { it.type?.isFlexibleRecursive() == true }
            }
            return false
        }
    }
}