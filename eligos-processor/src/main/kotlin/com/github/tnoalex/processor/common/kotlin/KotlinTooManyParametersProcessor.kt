package com.github.tnoalex.processor.common.kotlin

import com.github.tnoalex.foundation.bean.Component
import com.github.tnoalex.issues.common.ExcessiveParamsIssue
import com.github.tnoalex.processor.ShareSpace
import com.github.tnoalex.processor.SubProcessor
import com.github.tnoalex.processor.common.TooManyParametersProcessor

import com.github.tnoalex.processor.utils.filePath
import com.github.tnoalex.processor.utils.nameCanNotResolveWarn
import com.github.tnoalex.processor.utils.startLine
import com.intellij.psi.PsiFile
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtTreeVisitorVoid
import org.slf4j.LoggerFactory

@Component
class KotlinTooManyParametersProcessor : SubProcessor {
    private lateinit var shareSpace: TooManyParametersProcessor.TooManyParametersShareSpace
    override fun process(psiFile: PsiFile, shareSpace: ShareSpace) {
        this.shareSpace = shareSpace as TooManyParametersProcessor.TooManyParametersShareSpace
        psiFile.accept(kotlinFunctionVisitor())
    }

    private fun kotlinFunctionVisitor(): KtTreeVisitorVoid {
        return object : KtTreeVisitorVoid() {
            override fun visitNamedFunction(function: KtNamedFunction) {
                if (function.valueParameters.size > shareSpace.shareArity) {
                    with(function) {
                        shareSpace.shareIssues.add(
                            ExcessiveParamsIssue(
                                filePath,
                                fqName?.asString() ?: let {
                                    logger.nameCanNotResolveWarn("function", this)
                                    "unknown func"
                                },
                                function.valueParameters.map { p ->
                                    p.name ?: let {
                                        logger.nameCanNotResolveWarn("parameter", p)
                                        "unknown param"
                                    }
                                },
                                function.startLine,
                                valueParameters.size
                            )
                        )
                    }
                }
                super.visitNamedFunction(function)
            }
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(KotlinTooManyParametersProcessor::class.java)
    }
}