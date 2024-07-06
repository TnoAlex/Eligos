package com.github.tnoalex.processor.common.kotlin

import com.github.tnoalex.foundation.bean.Component
import com.github.tnoalex.issues.common.UnusedImportIssue
import com.github.tnoalex.processor.ShareSpace
import com.github.tnoalex.processor.SubProcessor
import com.github.tnoalex.processor.common.UnUsedImportProcessor
import com.github.tnoalex.processor.utils.refCanNotResolveWarn
import com.github.tnoalex.processor.utils.referenceExpressionSelfOrInChildren
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiPackage
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.isInImportDirective
import org.slf4j.LoggerFactory
import java.util.HashMap
import java.util.HashSet

@Component
class KotlinUnUsedImportProcessor : SubProcessor {
    private lateinit var myShareSpace: UnUsedImportProcessor.UnUsedImportProcessorShareSpace
    override fun process(psiFile: PsiFile, shareSpace: ShareSpace) {
        myShareSpace = shareSpace as UnUsedImportProcessor.UnUsedImportProcessorShareSpace
        findKotlinUseLessImport(psiFile as KtFile)
    }

    private fun findKotlinUseLessImport(ktFile: KtFile) {
        val importList = PsiTreeUtil.getChildOfType(ktFile, KtImportList::class.java) ?: return
        val importsRefs = HashSet<PsiElement>()
        val importsMap = HashMap<PsiElement, String>()
        importList.accept(object : KtTreeVisitorVoid() {
            override fun visitReferenceExpression(expression: KtReferenceExpression) {
                val parentText =
                    PsiTreeUtil.getParentOfType(expression, KtImportDirective::class.java)!!.text.removePrefix("import")
                        .trim()
                if (parentText.contains("*")) { // import ccc.xxx.*
                    val lastPackage = parentText.removeSuffix(".*").split(".").last()
                    if (expression.text != lastPackage) return
                    (expression.references.first().resolve() as? PsiPackage)?.let {
                        importsRefs.add(it)
                        importsMap[it] = parentText
                    }
                }
                if (parentText.endsWith(expression.text)) { // import ccc.xx.AA
                    expression.references.first().resolve()?.let {
                        importsRefs.add(it)
                        importsMap[it] = parentText
                    }
                }
            }
        })


        ktFile.accept(object : KtTreeVisitorVoid() {
            override fun visitReferenceExpression(expression: KtReferenceExpression) {
                if (expression.isInImportDirective())
                    return super.visitReferenceExpression(expression)
                if (PsiTreeUtil.getParentOfType(expression, KtPackageDirective::class.java) != null)
                    return super.visitReferenceExpression(expression)
                expression.referenceExpressionSelfOrInChildren().forEach {
                    try {
                        it.references.forEach { ref ->
                            ref.resolve()?.let { r ->
                                myShareSpace.resolveImports(r, importsRefs)
                            }
                        }
                    } catch (e: RuntimeException) {
                        logger.refCanNotResolveWarn(expression)
                    }
                }
                super.visitReferenceExpression(expression)
            }
        })
        if (importsRefs.isNotEmpty()) {
            myShareSpace.shareIssues.add(
                UnusedImportIssue(
                    hashSetOf(ktFile.virtualFilePath),
                    importsRefs.map { importsMap[it]!! })
            )
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(KotlinUnUsedImportProcessor::class.java)
    }
}