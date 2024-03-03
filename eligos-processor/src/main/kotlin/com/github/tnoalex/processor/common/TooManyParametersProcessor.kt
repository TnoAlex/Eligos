package com.github.tnoalex.processor.common

import com.github.tnoalex.Context
import com.github.tnoalex.config.WiredConfig
import com.github.tnoalex.foundation.ApplicationContext
import com.github.tnoalex.foundation.LaunchEnvironment
import com.github.tnoalex.foundation.bean.Component
import com.github.tnoalex.foundation.bean.Suitable
import com.github.tnoalex.foundation.eventbus.EventListener
import com.github.tnoalex.issues.ExcessiveParamsIssue
import com.github.tnoalex.processor.PsiProcessor
import com.intellij.psi.*
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtTreeVisitorVoid
import java.util.*

@Component
@Suitable(LaunchEnvironment.CLI)
class TooManyParametersProcessor : PsiProcessor {
    private val issues = LinkedList<ExcessiveParamsIssue>()

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
        ApplicationContext.getExactBean(Context::class.java)!!.reportIssues(issues)
        issues.clear()
    }

    private fun javaFunctionVisitor(): JavaRecursiveElementVisitor {
        return object : JavaRecursiveElementVisitor() {
            override fun visitMethod(method: PsiMethod) {
                if (method.parameters.size >= arity) {
                    with(method) {
                        issues.add(ExcessiveParamsIssue(containingFile.virtualFile.path, name, parameters.size))
                    }
                }
            }
        }
    }

    private fun kotlinFunctionVisitor(): KtTreeVisitorVoid {
        return object : KtTreeVisitorVoid() {
            override fun visitNamedFunction(function: KtNamedFunction) {
                if (function.valueParameters.size > arity) {
                    with(function) {
                        issues.add(ExcessiveParamsIssue(containingFile.virtualFile.path, name!!, valueParameters.size))
                    }
                }
            }
        }
    }
}