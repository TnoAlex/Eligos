package com.github.tnoalex.processor.kotlin

import com.github.tnoalex.foundation.LaunchEnvironment
import com.github.tnoalex.foundation.bean.Component
import com.github.tnoalex.foundation.bean.Suitable
import com.github.tnoalex.foundation.eventbus.EventListener
import com.github.tnoalex.issues.kotlin.ImplicitSingleExprFunctionIssue
import com.github.tnoalex.processor.PsiProcessor
import com.github.tnoalex.processor.utils.resolveToDescriptorIfAny
import com.github.tnoalex.processor.utils.startLine
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.types.checker.SimpleClassicTypeSystemContext.isUnit
import org.slf4j.LoggerFactory


@Component
@Suitable(LaunchEnvironment.CLI)
class ImplicitSingleExprFunctionProcessor : PsiProcessor {
    override val supportLanguage: List<String>
        get() = listOf("kotlin")

    @EventListener
    fun process(ktFile: KtFile) {
        ktFile.accept(object : KtTreeVisitorVoid() {
            override fun visitNamedFunction(function: KtNamedFunction) {
                if (PsiTreeUtil.getChildOfType(function, KtTypeReference::class.java) != null) return
                if (PsiTreeUtil.getChildOfType(function, KtBlockExpression::class.java) != null) return
                val returnType = function.resolveToDescriptorIfAny()?.returnType
                    ?: let {
                        logger.warn("Unknown return type of function in ${ktFile.name} at line ${function.startLine}")
                        return
                    }
                if (returnType.unwrap().isUnit()) return
                context.reportIssue(
                    ImplicitSingleExprFunctionIssue(
                        ktFile.virtualFilePath,
                        function.fqName?.asString() ?: let {
                            logger.warn("Unknown function name in file ${function.containingFile.name} at line ${function.startLine}")
                            "unknown func"
                        },
                        function.valueParameters.map {
                            it.name ?: let {
                                logger.warn("Unknown parameter in function ${function.name} of file ${function.containingFile.name} " +
                                        "at line ${function.startLine}")
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