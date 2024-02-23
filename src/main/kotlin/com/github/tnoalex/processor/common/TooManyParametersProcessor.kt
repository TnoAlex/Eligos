package com.github.tnoalex.processor.common

import com.github.tnoalex.AnalysisHierarchyEnum
import com.github.tnoalex.Context
import com.github.tnoalex.config.WiredConfig
import com.github.tnoalex.events.EntityRepoFinishedEvent
import com.github.tnoalex.foundation.ApplicationContext
import com.github.tnoalex.foundation.bean.Component
import com.github.tnoalex.foundation.eventbus.EventListener
import com.github.tnoalex.issues.ExcessiveParamsIssue
import com.github.tnoalex.processor.PsiProcessor
import com.github.tnoalex.utils.getEntitiesByType
import depends.entity.FunctionEntity
import org.jetbrains.kotlin.com.intellij.psi.*
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtTreeVisitorVoid
import java.util.*

@Component
class TooManyParametersProcessor : PsiProcessor {
    private val issues = LinkedList<ExcessiveParamsIssue>()

    @WiredConfig("function.arity")
    private var arity: Int = 0


    @EventListener
    fun process(event: EntityRepoFinishedEvent) {
        findTooManyParameters(event.source as Context)
        (event.source as Context).reportIssues(issues)
        issues.clear()
    }

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

    private fun findTooManyParameters(context: Context) {
        val functionEntities = context.getRepo().getEntitiesByType(FunctionEntity::class.java)
        val functionDependency = context.getDependencyMatrix(AnalysisHierarchyEnum.METHOD)
        functionEntities.map { it as FunctionEntity }.filter {
            it.parameters.size > arity
        }.forEach {
            val file = functionDependency!!.nodes.first { f ->
                f.split("(")[1] == it.qualifiedName + ")"
            }.split("(")[0]
            val params = HashMap<String, String>()
            it.parameters.forEach { p ->
                if (p.rawType == null) return@forEach
                params[p.rawName.name] = p.rawType.name
            }
        }
    }
}