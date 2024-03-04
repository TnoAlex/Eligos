package com.github.tnoalex.processor.common

import com.github.tnoalex.Context
import com.github.tnoalex.foundation.ApplicationContext
import com.github.tnoalex.foundation.LaunchEnvironment
import com.github.tnoalex.foundation.bean.Component
import com.github.tnoalex.foundation.bean.Suitable
import com.github.tnoalex.foundation.eventbus.EventListener
import com.github.tnoalex.issues.UnusedImportIssue
import com.github.tnoalex.processor.PsiProcessor
import com.intellij.psi.*
import com.intellij.psi.impl.compiled.ClsFileImpl
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.analysis.decompiler.psi.file.KtDecompiledFile
import org.jetbrains.kotlin.asJava.elements.KtLightElement
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.isInImportDirective
import org.jetbrains.kotlin.psi.psiUtil.referenceExpression
import java.util.*

@Component
@Suitable(LaunchEnvironment.CLI)
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
        ApplicationContext.getExactBean(Context::class.java)!!.reportIssues(issues)
        issues.clear()
    }

    private fun findJavaUseLessImport(javaFile: PsiJavaFile) {
        val importList = PsiTreeUtil.getChildOfType(javaFile, PsiImportList::class.java) ?: return
        val importRefs = HashSet<PsiElement>()
        val importsMap = HashMap<PsiElement, String>()

        importList.importStatements.forEach {
            it.importReference?.resolve()?.let { r ->
                if (r is KtLightElement<*, *>) {
                    importRefs.add(r.kotlinOrigin!!)
                    importsMap[r.kotlinOrigin!!] = it.text.removePrefix("import").trim()
                }
                importRefs.add(r)
                importsMap[r] = it.text.removePrefix("import").trim()
            }
        }
        javaFile.acceptChildren(object : JavaRecursiveElementVisitor() {
            override fun visitReferenceElement(reference: PsiJavaCodeReferenceElement) {
                if (PsiTreeUtil.getParentOfType(reference, PsiPackageStatement::class.java) != null) return
                if (PsiTreeUtil.getParentOfType(reference, PsiImportStatement::class.java) != null) return
                reference.resolve()?.let {
                    if (it is KtLightElement<*, *>) {
                        resolveImports(it.kotlinOrigin!!, importRefs)
                    } else resolveImports(it, importRefs)
                }
            }
        })

        issues.add(UnusedImportIssue(hashSetOf(javaFile.virtualFile.path), importRefs.map { importsMap[it]!! }))
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
                if (expression.isInImportDirective()) return
                if (PsiTreeUtil.getParentOfType(expression, KtPackageDirective::class.java) != null) return
                expression.referenceExpression()?.run {
                    references.forEach {
                        it.resolve()?.let { r ->
                            resolveImports(r, importsRefs)
                        }
                    }
                }
            }
        })

        issues.add(UnusedImportIssue(hashSetOf(ktFile.virtualFilePath), importsRefs.map { importsMap[it]!! }))
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
}