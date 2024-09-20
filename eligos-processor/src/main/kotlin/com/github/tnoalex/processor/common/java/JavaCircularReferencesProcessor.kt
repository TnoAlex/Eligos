package com.github.tnoalex.processor.common.java

import com.github.tnoalex.foundation.LaunchEnvironment
import com.github.tnoalex.foundation.bean.Component
import com.github.tnoalex.foundation.bean.Suitable
import com.github.tnoalex.foundation.language.JavaLanguage
import com.github.tnoalex.foundation.language.KotlinLanguage
import com.github.tnoalex.foundation.language.Language
import com.github.tnoalex.processor.ShareSpace
import com.github.tnoalex.processor.SubProcessor
import com.github.tnoalex.processor.common.CircularReferencesProcessor
import com.github.tnoalex.processor.utils.refCanNotResolveWarn
import com.intellij.psi.*
import com.intellij.psi.util.PsiTreeUtil
import org.slf4j.LoggerFactory

@Component
@Suitable(LaunchEnvironment.CLI)
class JavaCircularReferencesProcessor : SubProcessor {
    override val supportLanguage: List<Language>
        get() = listOf(JavaLanguage)

    override fun process(psiFile: PsiFile, shareSpace: ShareSpace) {
        handleJavaFile(psiFile as PsiJavaFile, shareSpace as CircularReferencesProcessor.CircularReferencesShareSpace)
    }

    private fun handleJavaFile(
        javaFile: PsiJavaFile,
        shareSpace: CircularReferencesProcessor.CircularReferencesShareSpace
    ) {
        val fileName = javaFile.virtualFile.path
        shareSpace.shareDependencyGraph.addVertex(fileName)
        javaFile.accept(object : JavaRecursiveElementVisitor() {
            override fun visitReferenceElement(reference: PsiJavaCodeReferenceElement) {
                if (PsiTreeUtil.getParentOfType(reference, PsiPackageStatement::class.java) != null)
                    return super.visitReferenceElement(reference)
                if (PsiTreeUtil.getParentOfType(reference, PsiImportStatement::class.java) != null)
                    return super.visitReferenceElement(reference)
                try {
                    reference.resolve()?.let {
                        shareSpace.resolveRef(it, fileName)
                    }
                } catch (e: RuntimeException) {
                    logger.refCanNotResolveWarn(reference)
                }
                super.visitReferenceElement(reference)
            }
        })
    }

    companion object {
        private val logger = LoggerFactory.getLogger(JavaCircularReferencesProcessor::class.java)
    }
}