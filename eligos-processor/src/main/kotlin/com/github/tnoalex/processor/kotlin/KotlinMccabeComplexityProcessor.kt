package com.github.tnoalex.processor.kotlin

import com.github.tnoalex.config.WiredConfig
import com.github.tnoalex.foundation.LaunchEnvironment
import com.github.tnoalex.foundation.bean.Component
import com.github.tnoalex.foundation.bean.Suitable
import com.github.tnoalex.foundation.eventbus.EventListener
import com.github.tnoalex.issues.kotlin.ComplexKotlinFunctionIssue
import com.github.tnoalex.processor.PsiProcessor
import com.github.tnoalex.processor.utils.startLine
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.*
import org.slf4j.LoggerFactory

@Component
@Suitable(LaunchEnvironment.CLI)
class KotlinMccabeComplexityProcessor : PsiProcessor {
    @WiredConfig("function.maxCyclomaticComplexity")
    private var maxCyclomaticComplexity = 0
    private var currentComplexity = 1

    @EventListener
    fun process(ktFile: KtFile) {
        ktFile.accept(object : KtTreeVisitorVoid() {
            override fun visitNamedFunction(function: KtNamedFunction) {
                function.accept(ktCComplexityVisitor)
                if (currentComplexity >= maxCyclomaticComplexity) {
                    context.reportIssue(
                        ComplexKotlinFunctionIssue(
                            ktFile.virtualFilePath,
                            function.fqName?.asString() ?: let {
                                logger.warn("Unknown function name in file ${function.containingFile.name} at line ${function.startLine}")
                                "unknown func"
                            },
                            function.valueParameters.map {
                                it.name ?: let {
                                    logger.warn(
                                        "Unknown parameter in function ${function.name} of file ${function.containingFile.name} " +
                                                "at line ${function.startLine}"
                                    )
                                    ""
                                }
                            },
                            function.startLine,
                            currentComplexity
                        )
                    )
                }
                currentComplexity = 1
            }
        })
    }

    private fun computeConditionComplexity(condition: KtExpression?) {
        condition?.acceptChildren(object : KtTreeVisitorVoid() {
            override fun visitReferenceExpression(expression: KtReferenceExpression) {
                if (expression is KtOperationReferenceExpression) {
                    if (expression.operationSignTokenType == KtTokens.ANDAND
                        || expression.operationSignTokenType == KtTokens.OROR
                    ) {
                        currentComplexity++
                    }
                }
            }
        })
    }

    private val ktCComplexityVisitor = object : KtTreeVisitorVoid() {
        override fun visitForExpression(expression: KtForExpression) {
            currentComplexity++
            super.visitForExpression(expression)
        }

        override fun visitWhileExpression(expression: KtWhileExpression) {
            currentComplexity++
            computeConditionComplexity(expression.condition)
            super.visitWhileExpression(expression)
        }

        override fun visitIfExpression(expression: KtIfExpression) {
            currentComplexity++
            computeConditionComplexity(expression.condition)
            super.visitIfExpression(expression)
        }


        override fun visitWhenExpression(expression: KtWhenExpression) {
            currentComplexity += expression.entries.size
            super.visitWhenExpression(expression)
        }

        override fun visitTryExpression(expression: KtTryExpression) {
            currentComplexity++
            super.visitTryExpression(expression)
        }

        override fun visitDoWhileExpression(expression: KtDoWhileExpression) {
            currentComplexity++
            computeConditionComplexity(expression.condition)
            super.visitDoWhileExpression(expression)
        }

    }

    companion object {
        @JvmStatic
        private val logger = LoggerFactory.getLogger(KotlinMccabeComplexityProcessor::class.java)
    }
}