package com.github.tnoalex.processor.common

import com.github.tnoalex.foundation.LaunchEnvironment
import com.github.tnoalex.foundation.bean.Component
import com.github.tnoalex.foundation.bean.Suitable
import com.github.tnoalex.foundation.eventbus.EventListener
import com.github.tnoalex.issues.UnusedImportIssue
import com.github.tnoalex.processor.PsiProcessor
import com.intellij.psi.*
import com.intellij.psi.impl.compiled.ClsElementImpl
import com.intellij.psi.impl.compiled.ClsFileImpl
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.isInImportDirective
import org.jetbrains.kotlin.psi.psiUtil.referenceExpression
import java.util.*

@Component
@Suitable(LaunchEnvironment.IDE_PLUGIN)
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
                            if (!importsRefs.contains(r)) { //import from cc.zz.*
                                if (r is ClsElementImpl) { // lib import
                                    val libFile =
                                        PsiTreeUtil.getParentOfType(r, ClsFileImpl::class.java) ?: return@forEach
                                    importsRefs.removeIf { rf -> rf is PsiPackage && rf.qualifiedName == libFile.packageName }
                                } else { // src import
                                    PsiTreeUtil.getParentOfType(r, KtFile::class.java)?.let {
                                        importsRefs.removeIf { rf -> rf is PsiPackage && rf.qualifiedName == it.packageFqName.asString() }
                                        return@forEach
                                    }
                                    PsiTreeUtil.getParentOfType(r,PsiJavaFile::class.java)?.let {
                                        importsRefs.removeIf { rf -> rf is PsiPackage && rf.qualifiedName == it.packageName }
                                    }
                                }
                            } else importsRefs.remove(r) //import from cc.zz.AA
                        }
                    }
                }
            }
        })

        issues.add(UnusedImportIssue(hashSetOf(ktFile.virtualFilePath), importsRefs.map { importsMap[it]!! }))
    }
}