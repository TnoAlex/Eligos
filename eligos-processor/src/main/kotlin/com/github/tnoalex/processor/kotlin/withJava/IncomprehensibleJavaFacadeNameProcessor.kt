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
import com.intellij.psi.PsiFile
import org.jetbrains.kotlin.analysis.api.symbols.KaSymbolVisibility
import org.jetbrains.kotlin.fileClasses.javaFileFacadeFqName
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.psiUtil.getChildrenOfType

@Component
@Suitable(LaunchEnvironment.CLI)
class IncomprehensibleJavaFacadeNameProcessor : IssueProcessor {
    override val severity: Severity = Severity.SUGGESTION
    override val supportLanguage: List<Language> = listOf(JavaLanguage, KotlinLanguage)

    @EventListener(filterClazz = [KtFile::class])
    override fun process(psiFile: PsiFile) {
        psiFile as KtFile
        analyze {
            val javaFacadeName = psiFile.javaFileFacadeFqName.shortName().asString()
            if (!javaFacadeName.endsWith("Kt")) return@analyze
            val namedFunctions = psiFile.getChildrenOfType<KtNamedFunction>()
                .filter { it.symbol.visibility == KaSymbolVisibility.PUBLIC }
            val ktProperties = psiFile.getChildrenOfType<KtProperty>()
                .filter { it.symbol.visibility == KaSymbolVisibility.PUBLIC }
            if (namedFunctions.isEmpty() && ktProperties.isEmpty()) return@analyze

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
}