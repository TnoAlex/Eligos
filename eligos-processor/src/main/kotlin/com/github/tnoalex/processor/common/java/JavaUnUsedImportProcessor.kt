package com.github.tnoalex.processor.common.java

import com.github.tnoalex.foundation.bean.Component
import com.github.tnoalex.issues.common.UnusedImportIssue
import com.github.tnoalex.processor.ShareSpace
import com.github.tnoalex.processor.SubProcessor
import com.github.tnoalex.processor.common.UnUsedImportProcessor
import com.github.tnoalex.processor.utils.refCanNotResolveWarn
import com.intellij.psi.*
import com.intellij.psi.util.PsiTreeUtil
import org.slf4j.LoggerFactory
import java.util.HashMap
import java.util.HashSet

@Component
class JavaUnUsedImportProcessor : SubProcessor {
    private lateinit var myShareSpace: UnUsedImportProcessor.UnUsedImportProcessorShareSpace
    override fun process(psiFile: PsiFile, shareSpace: ShareSpace) {
        myShareSpace = shareSpace as UnUsedImportProcessor.UnUsedImportProcessorShareSpace
        findJavaUseLessImport(psiFile as PsiJavaFile)
    }

    private fun findJavaUseLessImport(javaFile: PsiJavaFile) {
        val importList = PsiTreeUtil.getChildOfType(javaFile, PsiImportList::class.java) ?: return
        val importRefs = HashSet<PsiElement>()
        val importsMap = HashMap<PsiElement, String>()

        importList.importStatements.forEach {
            it.importReference?.resolve()?.let { r ->
                importRefs.add(r)
                importsMap[r] = it.text.removePrefix("import").trim()
            }
        }
        javaFile.acceptChildren(object : JavaRecursiveElementVisitor() {
            override fun visitReferenceElement(reference: PsiJavaCodeReferenceElement) {
                if (PsiTreeUtil.getParentOfType(reference, PsiPackageStatement::class.java) != null)
                    return super.visitReferenceElement(reference)
                if (PsiTreeUtil.getParentOfType(reference, PsiImportStatement::class.java) != null)
                    return super.visitReferenceElement(reference)
                try {
                    reference.resolve()?.let {
                        myShareSpace.resolveImports(it, importRefs)
                    }
                } catch (e: RuntimeException) {
                    logger.refCanNotResolveWarn(reference)
                }
                super.visitReferenceElement(reference)
            }
        })
        if (importRefs.isNotEmpty()) {
            myShareSpace.shareIssues.add(
                UnusedImportIssue(
                    hashSetOf(javaFile.virtualFile.path),
                    importRefs.map { importsMap[it]!! })
            )
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(JavaUnUsedImportProcessor::class.java)
    }
}