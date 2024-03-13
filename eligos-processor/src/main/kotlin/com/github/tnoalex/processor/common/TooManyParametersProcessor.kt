package com.github.tnoalex.processor.common

import com.github.tnoalex.config.WiredConfig
import com.github.tnoalex.foundation.LaunchEnvironment
import com.github.tnoalex.foundation.bean.Component
import com.github.tnoalex.foundation.bean.Suitable
import com.github.tnoalex.foundation.eventbus.EventListener
import com.github.tnoalex.issues.common.ExcessiveParamsIssue
import com.github.tnoalex.processor.PsiProcessor
import com.github.tnoalex.processor.utils.startLine
import com.intellij.psi.*
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtTreeVisitorVoid
import org.slf4j.LoggerFactory
import java.util.*

@Component
@Suitable(LaunchEnvironment.CLI)
class TooManyParametersProcessor : PsiProcessor {
    private val issues = LinkedList<ExcessiveParamsIssue>()
    override val supportLanguage: List<String>
        get() = listOf("java", "kotlin")

    @WiredConfig("function.arity")
    private var arity: Int = 0

    @EventListener
    fun process(psiFile: PsiFile) {
        when (psiFile) {
            is PsiJavaFile -> {
                psiFile.accept(javaFunctionVisitor())
            }

            is KtFile -> {
                psiFile.accept(kotlinFunctionVisitor())
            }
        }
        context.reportIssues(issues)
        issues.clear()
    }

    private fun javaFunctionVisitor(): JavaRecursiveElementVisitor {
        return object : JavaRecursiveElementVisitor() {
            override fun visitMethod(method: PsiMethod) {
                if (method.parameters.size >= arity) {
                    with(method) {
                        issues.add(
                            ExcessiveParamsIssue(
                                containingFile.virtualFile.path, name,
                                method.parameters.map { (it as PsiParameter).text},
                                method.startLine,
                                parameters.size
                            )
                        )
                    }
                }
                super.visitMethod(method)
            }
        }
    }

    private fun kotlinFunctionVisitor(): KtTreeVisitorVoid {
        return object : KtTreeVisitorVoid() {
            override fun visitNamedFunction(function: KtNamedFunction) {
                if (function.valueParameters.size > arity) {
                    with(function) {
                        issues.add(
                            ExcessiveParamsIssue(
                                containingFile.virtualFile.path,
                                fqName?.asString() ?: let {
                                    logger.warn("Unknown function name in ${function.containingFile.name} at line ${function.startLine}")
                                    "unknown func"
                                },
                                function.valueParameters.map {
                                    it.name ?: let {
                                        logger.warn("Unknown parameter name in ${function.name} of file ${function.containingFile.name} " +
                                                "at line ${function.startLine} ")
                                        ""
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
        @JvmStatic
        private val logger = LoggerFactory.getLogger(TooManyParametersProcessor::class.java)
    }
}