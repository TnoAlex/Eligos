package com.github.tnoalex.processor.kotlin.withJava

import com.github.tnoalex.foundation.LaunchEnvironment
import com.github.tnoalex.foundation.bean.Component
import com.github.tnoalex.foundation.bean.Suitable
import com.github.tnoalex.foundation.eventbus.EventListener
import com.github.tnoalex.foundation.language.JavaLanguage
import com.github.tnoalex.foundation.language.KotlinLanguage
import com.github.tnoalex.foundation.language.Language
import com.github.tnoalex.issues.Severity
import com.github.tnoalex.issues.kotlin.withJava.IncomprehensibleJavaFacadeNameIssue
import com.github.tnoalex.processor.IssueProcessor
import com.github.tnoalex.processor.utils.filePath
import com.github.tnoalex.processor.utils.resolveToDescriptorIfAny
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiJavaFile
import org.jetbrains.kotlin.fileClasses.javaFileFacadeFqName
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.psiUtil.getChildrenOfType
import org.jetbrains.kotlin.resolve.descriptorUtil.isEffectivelyPublicApi
import org.jetbrains.kotlin.utils.addToStdlib.ifFalse

@Component
@Suitable(LaunchEnvironment.CLI)
class IncomprehensibleJavaFacadeNameProcessor : IssueProcessor {
    override val severity: Severity
        get() = Severity.SUGGESTION
    override val supportLanguage: List<Language>
        get() = listOf(JavaLanguage, KotlinLanguage)

    @EventListener(filterClazz = [KtFile::class])
    override fun process(psiFile: PsiFile) {
        psiFile as KtFile
        val namedFunctions = psiFile.getChildrenOfType<KtNamedFunction>()
            .filter { it.resolveToDescriptorIfAny()?.isEffectivelyPublicApi == true }
        val ktProperties = psiFile.getChildrenOfType<KtProperty>()
            .filter { it.resolveToDescriptorIfAny()?.isEffectivelyPublicApi == true }
        if (namedFunctions.isEmpty() && ktProperties.isEmpty()) return
        val javaFacadeName = psiFile.javaFileFacadeFqName.shortName().asString()
        javaFacadeName.endsWith("Kt").ifFalse { return }
        context.reportIssue(
            IncomprehensibleJavaFacadeNameIssue(
                psiFile.filePath,
                javaFacadeName,
                ktProperties.isNotEmpty(),
                namedFunctions.isNotEmpty()
            )
        )
    }
}