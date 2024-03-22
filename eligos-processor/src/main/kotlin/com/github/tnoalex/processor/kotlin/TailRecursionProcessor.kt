package com.github.tnoalex.processor.kotlin

import com.github.tnoalex.foundation.LaunchEnvironment
import com.github.tnoalex.foundation.bean.Component
import com.github.tnoalex.foundation.bean.Suitable
import com.github.tnoalex.foundation.eventbus.EventListener
import com.github.tnoalex.issues.kotlin.OptimizedTailRecursionIssue
import com.github.tnoalex.processor.PsiProcessor
import com.github.tnoalex.processor.utils.referenceExpressionSelfOrInChildren
import com.github.tnoalex.processor.utils.startLine
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.*
import org.slf4j.LoggerFactory


@Component
@Suitable(LaunchEnvironment.CLI)
class TailRecursionProcessor : PsiProcessor {
    override val supportLanguage: List<String>
        get() = listOf("kotlin")

    @EventListener
    fun process(ktFile: KtFile) {
        ktFile.accept(object : KtTreeVisitorVoid() {
            override fun visitNamedFunction(function: KtNamedFunction) {
                if (function.hasModifier(KtTokens.TAILREC_KEYWORD)) return
                val isTailRecursion = findRecursion(function)
                if (isTailRecursion) {
                    context.reportIssue(
                        OptimizedTailRecursionIssue(
                            ktFile.virtualFilePath,
                            function.fqName?.asString() ?: let {
                                logger.warn("Unknown function name in ${ktFile.name} at line ${function.startLine}")
                                "unknown func"
                            },
                            function.valueParameters.map {
                                it.name ?: let {
                                    logger.warn(
                                        "Unknown parameter name in ${function.name} of file ${function.containingFile.name}" +
                                                " at line ${function.startLine}"
                                    )
                                    ""
                                }
                            },
                            function.startLine
                        )
                    )
                }
                super.visitNamedFunction(function)
            }
        })
    }

    private fun findRecursion(function: KtNamedFunction): Boolean {
        var isTailRecursion = false
        var foundReturnExpression = false
        function.acceptChildren(object : KtTreeVisitorVoid() {
            override fun visitReturnExpression(expression: KtReturnExpression) {
                if (isTailRecursion) return
                foundReturnExpression = true
                val callExpressions = ArrayList<KtCallExpression>()
                expression.acceptChildren(object : KtTreeVisitorVoid() {
                    override fun visitCallExpression(expression: KtCallExpression) {
                        callExpressions.add(expression)
                    }
                })
                if (callExpressions.isNotEmpty()) {
                    var containsOtherCall = false
                    callExpressions.forEach loop@{
                        it.referenceExpressionSelfOrInChildren().forEach { ref->
                            ref.references.forEach innerLoop@ { r ->
                                val resolve = r.resolve() ?: let {
                                    containsOtherCall = true
                                    return@innerLoop
                                }
                                if (resolve == function) {
                                    PsiTreeUtil.getParentOfType(it, KtOperationExpression::class.java)?.let {
                                        return
                                    }
                                    isTailRecursion = true
                                    return
                                } else containsOtherCall = true
                            }
                        }
                    }
                    if (!containsOtherCall) isTailRecursion = true
                }
            }
        })
        return isTailRecursion && foundReturnExpression
    }

    companion object {
        @JvmStatic
        private val logger = LoggerFactory.getLogger(TailRecursionProcessor::class.java)
    }
}