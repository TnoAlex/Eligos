package com.github.tnoalex.processor.common

import com.github.tnoalex.foundation.bean.Component
import com.github.tnoalex.foundation.eventbus.EventListener
import com.github.tnoalex.issues.UnusedImportIssue
import com.github.tnoalex.processor.PsiProcessor
import com.intellij.psi.*
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.psi.*
import java.util.*

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
        ktFile.accept(object : KtTreeVisitorVoid() {
            override fun visitElement(element: PsiElement) {
                println(element.reference)
                println(element.text)
                super.visitElement(element)
            }

            override fun visitExpression(expression: KtExpression) {
                super.visitExpression(expression)
            }

            override fun visitReferenceExpression(expression: KtReferenceExpression) {
                if (expression is KtCallExpression) {
                }
                println()
                super.visitReferenceExpression(expression)
            }
        })
        importList.imports.forEach {
            println()
        }
    }
}