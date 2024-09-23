package com.github.tnoalex.processor.common

import com.github.tnoalex.foundation.LaunchEnvironment
import com.github.tnoalex.foundation.bean.Component
import com.github.tnoalex.foundation.bean.Suitable
import com.github.tnoalex.foundation.bean.inject.InjectBean
import com.github.tnoalex.foundation.eventbus.EventListener
import com.github.tnoalex.foundation.language.JavaLanguage
import com.github.tnoalex.foundation.language.KotlinLanguage
import com.github.tnoalex.foundation.language.Language
import com.github.tnoalex.issues.Severity
import com.github.tnoalex.issues.common.UnusedImportIssue
import com.github.tnoalex.processor.ShareSpace
import com.github.tnoalex.processor.common.providers.UnUsedImportProcessorProvider
import com.intellij.psi.*
import com.intellij.psi.impl.compiled.ClsFileImpl
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.analysis.decompiler.psi.file.KtDecompiledFile
import org.jetbrains.kotlin.psi.*
import java.util.*

@Component
@Suitable(LaunchEnvironment.CLI)
class UnUsedImportProcessor : AbstractCommonProcessor() {
    private val issues = LinkedList<UnusedImportIssue>()

    @InjectBean(beanType = UnUsedImportProcessorProvider::class)
    override lateinit var processorProvider: AbstractSpecificProcessorProvider

    override val severity: Severity = Severity.CODE_SMELL

    override val supportLanguage: List<Language> = listOf(JavaLanguage, KotlinLanguage)

    private val myShareSpace = UnUsedImportProcessorShareSpace()

    override fun createShearSpace(): ShareSpace = myShareSpace

    @EventListener(filterClazz = [PsiJavaFile::class, KtFile::class])
    override fun process(psiFile: PsiFile) {
        invokeSpecificProcessor(psiFile)
        context.reportIssues(issues)
        issues.clear()
    }

    internal inner class UnUsedImportProcessorShareSpace : ShareSpace {
        internal val shareIssues: MutableList<UnusedImportIssue>
            get() = issues

        internal fun resolveImports(element: PsiElement, importsRefs: HashSet<PsiElement>) {
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
}