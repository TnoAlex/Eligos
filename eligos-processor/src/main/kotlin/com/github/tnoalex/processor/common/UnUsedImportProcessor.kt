package com.github.tnoalex.processor.common

import com.github.tnoalex.foundation.LaunchEnvironment
import com.github.tnoalex.foundation.bean.Component
import com.github.tnoalex.foundation.bean.Suitable
import com.github.tnoalex.foundation.eventbus.EventListener
import com.github.tnoalex.issues.common.UnusedImportIssue
import com.github.tnoalex.processor.PsiProcessor
import com.github.tnoalex.processor.utils.refCanNotResolveWarn
import com.github.tnoalex.processor.utils.referenceExpressionSelfOrInChildren
import com.github.tnoalex.processor.utils.startLine
import com.intellij.psi.*
import com.intellij.psi.impl.compiled.ClsFileImpl
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.analysis.decompiler.psi.file.KtDecompiledFile
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.isInImportDirective
import org.slf4j.LoggerFactory
import java.util.*

@Component
@Suitable(LaunchEnvironment.CLI)
class UnUsedImportProcessor : PsiProcessor {
    private val issues = LinkedList<UnusedImportIssue>()
    override val supportLanguage: List<String>
        get() = listOf("java", "kotlin")

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
        context.reportIssues(issues)
        issues.clear()
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
                        resolveImports(it, importRefs)
                    }
                } catch (e: RuntimeException) {
                    logger.refCanNotResolveWarn(reference)
                }
                super.visitReferenceElement(reference)
            }
        })
        if (importRefs.isNotEmpty()) {
            issues.add(UnusedImportIssue(hashSetOf(javaFile.virtualFile.path), importRefs.map { importsMap[it]!! }))
        }
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
                                resolveImports(r, importsRefs)
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
            issues.add(UnusedImportIssue(hashSetOf(ktFile.virtualFilePath), importsRefs.map { importsMap[it]!! }))
        }
    }


    private fun resolveImports(element: PsiElement, importsRefs: HashSet<PsiElement>) {
        if (!importsRefs.contains(element)) { //import from cc.zz.*
            if (element is PsiCompiledElement) { // lib import
                PsiTreeUtil.getParentOfType(element, ClsFileImpl::class.java)?.let {
                    importsRefs.removeIf { rf -> rf is PsiPackage && rf.qualifiedName == it.packageName }
                    return
                }
                PsiTreeUtil.getParentOfType(element, KtDecompiledFile::class.java)?.let {
                    importsRefs.removeIf { rf -> rf is PsiPackage && rf.qualifiedName == it.packageFqName.asString() }
                    return
                }

            } else { // src import
                PsiTreeUtil.getParentOfType(element, KtFile::class.java)?.let {
                    importsRefs.removeIf { rf -> rf is PsiPackage && rf.qualifiedName == it.packageFqName.asString() }
                    return
                }
                PsiTreeUtil.getParentOfType(element, PsiJavaFile::class.java)?.let {
                    importsRefs.removeIf { rf -> rf is PsiPackage && rf.qualifiedName == it.packageName }
                }
            }
        } else importsRefs.remove(element) //import from cc.zz.AA
    }

    companion object {
        private val logger = LoggerFactory.getLogger(UnUsedImportProcessor::class.java)
    }
}