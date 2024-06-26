package com.github.tnoalex.processor.kotlin.withJava

import com.github.tnoalex.foundation.LaunchEnvironment
import com.github.tnoalex.foundation.bean.Component
import com.github.tnoalex.foundation.bean.Suitable
import com.github.tnoalex.foundation.eventbus.EventListener
import com.github.tnoalex.issues.Severity
import com.github.tnoalex.issues.kotlin.withJava.IncomprehensibleJavaFacadeNameIssue
import com.github.tnoalex.processor.PsiProcessor
import com.github.tnoalex.processor.utils.filePath
import com.github.tnoalex.processor.utils.resolveToDescriptorIfAny
import org.jetbrains.kotlin.fileClasses.javaFileFacadeFqName
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.psiUtil.getChildrenOfType
import org.jetbrains.kotlin.resolve.descriptorUtil.isEffectivelyPublicApi
import org.jetbrains.kotlin.utils.addToStdlib.ifFalse

@Component
@Suitable(LaunchEnvironment.CLI)
class IncomprehensibleJavaFacadeNameProcessor : PsiProcessor {
    override val severity: Severity
        get() = Severity.SUGGESTION
    override val supportLanguage: List<String>
        get() = listOf("kotlin", "java")

    @EventListener
    fun process(ktFile: KtFile) {
        val namedFunctions = ktFile.getChildrenOfType<KtNamedFunction>()
            .filter { it.resolveToDescriptorIfAny()?.isEffectivelyPublicApi == true }
        val ktProperties = ktFile.getChildrenOfType<KtProperty>()
            .filter { it.resolveToDescriptorIfAny()?.isEffectivelyPublicApi == true }
        if (namedFunctions.isEmpty() && ktProperties.isEmpty()) return
        val javaFacadeName = ktFile.javaFileFacadeFqName.shortName().asString()
        javaFacadeName.endsWith("Kt").ifFalse { return }
        context.reportIssue(
            IncomprehensibleJavaFacadeNameIssue(
                ktFile.filePath,
                javaFacadeName,
                ktProperties.isNotEmpty(),
                namedFunctions.isNotEmpty()
            )
        )
    }
}