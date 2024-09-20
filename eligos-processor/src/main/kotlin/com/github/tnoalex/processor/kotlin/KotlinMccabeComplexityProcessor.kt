package com.github.tnoalex.processor.kotlin

import com.github.tnoalex.config.InjectConfig
import com.github.tnoalex.foundation.LaunchEnvironment
import com.github.tnoalex.foundation.bean.Component
import com.github.tnoalex.foundation.bean.Suitable
import com.github.tnoalex.foundation.eventbus.EventListener
import com.github.tnoalex.foundation.language.JavaLanguage
import com.github.tnoalex.foundation.language.KotlinLanguage
import com.github.tnoalex.foundation.language.Language
import com.github.tnoalex.issues.Severity
import com.github.tnoalex.issues.kotlin.ComplexKotlinFunctionIssue
import com.github.tnoalex.processor.IssueProcessor
import com.github.tnoalex.processor.utils.nameCanNotResolveWarn
import com.github.tnoalex.processor.utils.startLine
import com.intellij.psi.PsiFile
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.*
import org.slf4j.LoggerFactory

@Component
@Suitable(LaunchEnvironment.CLI)
class KotlinMccabeComplexityProcessor : IssueProcessor {
    override val severity: Severity
        get() = Severity.CODE_SMELL
    override val supportLanguage: List<Language>
        get() = listOf(KotlinLanguage)

    @InjectConfig("function.maxCyclomaticComplexity")
    private var maxCyclomaticComplexity = 0
    private var currentComplexity = 1

    @EventListener(filterClazz = [KtFile::class])
    override fun process(psiFile: PsiFile) {
        psiFile.accept(object : KtTreeVisitorVoid() {
            override fun visitNamedFunction(function: KtNamedFunction) {
                function.accept(ktCComplexityVisitor)
                if (currentComplexity >= maxCyclomaticComplexity) {
                    context.reportIssue(
                        ComplexKotlinFunctionIssue(
                            function.containingKtFile.virtualFilePath,
                            function.fqName?.asString() ?: let {
                              logger.nameCanNotResolveWarn("function",function)
                                "unknown func"
                            },
                            function.valueParameters.map {
                                it.name ?: let {
                                   logger.nameCanNotResolveWarn("parameter",function)
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