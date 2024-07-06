package com.github.tnoalex.processor.common.kotlin

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
import com.github.tnoalex.processor.utils.referenceExpressionSelfOrInChildren
import com.intellij.psi.*
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtPackageDirective
import org.jetbrains.kotlin.psi.KtReferenceExpression
import org.jetbrains.kotlin.psi.KtTreeVisitorVoid
import org.jetbrains.kotlin.psi.psiUtil.isInImportDirective
import org.slf4j.LoggerFactory

@Component
@Suitable(LaunchEnvironment.CLI)
class KotlinCircularReferencesProcessor : SubProcessor {
    override val supportLanguage: List<Language>
        get() = listOf(KotlinLanguage)


    override fun process(psiFile: PsiFile, shareSpace: ShareSpace) {
        handleKtFile(psiFile as KtFile, shareSpace as CircularReferencesProcessor.CircularReferencesShareSpace)
    }

    private fun handleKtFile(ktFile: KtFile, shareSpace: CircularReferencesProcessor.CircularReferencesShareSpace) {
        val fileName = ktFile.virtualFilePath
        shareSpace.shareDependencyGraph.addVertex(fileName)
        ktFile.accept(object : KtTreeVisitorVoid() {
            override fun visitReferenceExpression(expression: KtReferenceExpression) {
                if (expression.isInImportDirective()) return super.visitReferenceExpression(expression)
                if (PsiTreeUtil.getParentOfType(expression, KtPackageDirective::class.java) != null)
                    return super.visitReferenceExpression(expression)
                expression.referenceExpressionSelfOrInChildren().forEach {
                    try {
                        it.references.forEach { ref ->
                            ref.resolve()?.let { r -> shareSpace.resolveRef(r, fileName) }
                        }
                    } catch (e: RuntimeException) {
                        logger.refCanNotResolveWarn(expression)
                    }
                }
                super.visitReferenceExpression(expression)
            }
        })
    }

    companion object {
        private val logger = LoggerFactory.getLogger(KotlinCircularReferencesProcessor::class.java)
    }
}