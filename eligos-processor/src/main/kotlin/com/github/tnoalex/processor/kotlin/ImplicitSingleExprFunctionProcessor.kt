package com.github.tnoalex.processor.kotlin

import com.github.tnoalex.foundation.LaunchEnvironment
import com.github.tnoalex.foundation.bean.Component
import com.github.tnoalex.foundation.bean.Suitable
import com.github.tnoalex.foundation.eventbus.EventListener
import com.github.tnoalex.issues.Severity
import com.github.tnoalex.issues.kotlin.ImplicitSingleExprFunctionIssue
import com.github.tnoalex.processor.PsiProcessor
import com.github.tnoalex.processor.utils.nameCanNotResolveWarn
import com.github.tnoalex.processor.utils.resolveToDescriptorIfAny
import com.github.tnoalex.processor.utils.startLine
import com.github.tnoalex.processor.utils.typeCanNotResolveWarn
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.types.checker.SimpleClassicTypeSystemContext.isUnit
import org.slf4j.LoggerFactory


@Component
@Suitable(LaunchEnvironment.CLI)
class ImplicitSingleExprFunctionProcessor : PsiProcessor {
    override val severity: Severity
        get() = Severity.CODE_SMELL
    override val supportLanguage: List<String>
        get() = listOf("kotlin")

    @EventListener
    fun process(ktFile: KtFile) {
        ktFile.accept(object : KtTreeVisitorVoid() {
            override fun visitNamedFunction(function: KtNamedFunction) {
                if (PsiTreeUtil.getChildOfType(
                        function,
                        KtTypeReference::class.java
                    ) != null
                ) return super.visitNamedFunction(function)
                if (PsiTreeUtil.getChildOfType(
                        function,
                        KtBlockExpression::class.java
                    ) != null
                ) return super.visitNamedFunction(function)
                val returnType = function.resolveToDescriptorIfAny()?.returnType
                    ?: let {
                        logger.typeCanNotResolveWarn("return", function)
                        return super.visitNamedFunction(function)
                    }
                if (returnType.unwrap().isUnit()) return super.visitNamedFunction(function)
                context.reportIssue(
                    ImplicitSingleExprFunctionIssue(
                        ktFile.virtualFilePath,
                        function.text,
                        function.fqName?.asString() ?: let {
                            logger.nameCanNotResolveWarn("function", function)
                            "unknown func"
                        },
                        function.valueParameters.map {
                            it.name ?: let {
                                logger.nameCanNotResolveWarn("parameter", function)
                                ""
                            }
                        },
                        function.startLine
                    )
                )
                super.visitNamedFunction(function)
            }
        })
    }

    companion object {
        @JvmStatic
        private val logger = LoggerFactory.getLogger(ImplicitSingleExprFunctionProcessor::class.java)
    }
}