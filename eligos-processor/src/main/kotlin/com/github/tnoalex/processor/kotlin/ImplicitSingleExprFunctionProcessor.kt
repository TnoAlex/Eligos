package com.github.tnoalex.processor.kotlin

import com.github.tnoalex.foundation.LaunchEnvironment
import com.github.tnoalex.foundation.bean.Component
import com.github.tnoalex.foundation.bean.Suitable
import com.github.tnoalex.foundation.eventbus.EventListener
import com.github.tnoalex.foundation.language.JavaLanguage
import com.github.tnoalex.foundation.language.KotlinLanguage
import com.github.tnoalex.foundation.language.Language
import com.github.tnoalex.issues.Severity
import com.github.tnoalex.issues.kotlin.ImplicitSingleExprFunctionIssue
import com.github.tnoalex.processor.IssueProcessor
import com.github.tnoalex.processor.utils.nameCanNotResolveWarn
import com.github.tnoalex.processor.utils.resolveToDescriptorIfAny
import com.github.tnoalex.processor.utils.startLine
import com.github.tnoalex.processor.utils.typeCanNotResolveWarn
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.types.checker.SimpleClassicTypeSystemContext.isUnit
import org.slf4j.LoggerFactory


@Component
@Suitable(LaunchEnvironment.CLI)
class ImplicitSingleExprFunctionProcessor : IssueProcessor {
    override val severity: Severity
        get() = Severity.CODE_SMELL
    override val supportLanguage: List<Language>
        get() = listOf(KotlinLanguage)

    @EventListener(filterClazz = [KtFile::class])
    override fun process(psiFile: PsiFile) {
        psiFile.accept(singleExprFunctionVisitor)
    }

    private val singleExprFunctionVisitor = object : KtTreeVisitorVoid(){
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
                    function.containingKtFile.virtualFilePath,
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
    }

    companion object {
        @JvmStatic
        private val logger = LoggerFactory.getLogger(ImplicitSingleExprFunctionProcessor::class.java)
    }
}