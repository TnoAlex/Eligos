package com.github.tnoalex.processor.common

import com.github.tnoalex.AnalysisHierarchyEnum
import com.github.tnoalex.Context
import com.github.tnoalex.events.EntityRepoFinishedEvent
import com.github.tnoalex.foundation.ApplicationContext
import com.github.tnoalex.foundation.bean.Component
import com.github.tnoalex.foundation.environment.JvmCompilerEnvironmentContext
import com.github.tnoalex.foundation.eventbus.EventListener
import com.github.tnoalex.issues.UnusedImportIssue
import com.github.tnoalex.processor.PsiProcessor
import depends.deptypes.DependencyType
import /*org.jetbrains.kotlin.*/com.intellij.psi.*
import /*org.jetbrains.kotlin.*/com.intellij.psi.codeStyle.CodeStyleManager
import /*org.jetbrains.kotlin.*/com.intellij.psi.search.PsiSearchScopeUtil
import /*org.jetbrains.kotlin.*/com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.calls.util.getResolvedCall
import org.jetbrains.kotlin.resolve.references.ReferenceAccess
import java.util.*
import kotlin.reflect.jvm.internal.impl.types.checker.KotlinTypeChecker

@Component
class UnUsedImportProcessor : PsiProcessor {
    private val issues = LinkedList<UnusedImportIssue>()

    @EventListener
    fun process(psiFile: PsiFile) {
        when (psiFile) {
            is PsiJavaFile -> {
                findJavaUseLessImport(psiFile)
            }

            is KtFile -> {
                findKotlinUseLessImport(psiFile)
            }
        }
    }

    private fun findJavaUseLessImport(javaFile: PsiJavaFile) {
        val importList = PsiTreeUtil.getChildOfType(javaFile, PsiImportList::class.java) ?: return
        importList.importStatements.forEach {
            val references = it.references
            println()
        }
    }

    private fun findKotlinUseLessImport(ktFile: KtFile) {
        val importList = PsiTreeUtil.getChildOfType(ktFile, KtImportList::class.java) ?: return
        val bindingContext = ApplicationContext.getExactBean(JvmCompilerEnvironmentContext::class.java)!!.bindingContext
        ktFile.accept(object : KtTreeVisitorVoid() {
            override fun visitElement(element: PsiElement) {
                println(element.reference)
                println(element.text)
                super.visitElement(element)
            }

            override fun visitExpression(expression: KtExpression) {
                val type = bindingContext.getType(expression)
                super.visitExpression(expression)
            }

            override fun visitReferenceExpression(expression: KtReferenceExpression) {
                if (expression is KtCallExpression) {
                    expression.getResolvedCall(bindingContext)
                }
                println()
                super.visitReferenceExpression(expression)
            }
        })
        importList.imports.forEach {
            println()
        }
    }

    fun process(event: EntityRepoFinishedEvent) {
        findUselessImport(event.source as Context)
        (event.source as Context).reportIssues(issues)
        issues.clear()
    }

    private fun findUselessImport(context: Context) {
        val fileDependency = context.getDependencyMatrix(AnalysisHierarchyEnum.FILE)
        fileDependency?.run {
            dependencyPairs.filter {
                it.dependencies.size == 1 && it.dependencies.first().type.equals(DependencyType.IMPORT)
            }.forEach {
                foundUnusedImportPattern(listOf(it.from, it.to), context)
            }
        }
    }

    private fun foundUnusedImportPattern(affectedFilesIndexes: List<Int>, context: Context) {
        val affectedFiles =
            affectedFilesIndexes.map { context.getDependencyMatrix(AnalysisHierarchyEnum.FILE)!!.getNodeName(it) }
        val issue = issues.filter { it.useFile == affectedFiles[0] }
        if (issue.isEmpty()) {
            issues.add(UnusedImportIssue(affectedFiles.toHashSet(), affectedFiles[0]))
        } else {
            issue[0].affectedFiles.addAll(affectedFiles)
            issues.remove(issue[0])
            issues.add(issue[0])
        }
    }

}