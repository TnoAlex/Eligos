package com.github.tnoalex.processor.kotlin.withJava

import com.github.tnoalex.foundation.LaunchEnvironment
import com.github.tnoalex.foundation.bean.Component
import com.github.tnoalex.foundation.bean.Suitable
import com.github.tnoalex.foundation.eventbus.EventListener
import com.github.tnoalex.foundation.language.JavaLanguage
import com.github.tnoalex.foundation.language.KotlinLanguage
import com.github.tnoalex.foundation.language.Language
import com.github.tnoalex.issues.Severity
import com.github.tnoalex.issues.kotlin.withJava.NonJVMFieldCompanionValueIssue
import com.github.tnoalex.processor.IssueProcessor
import com.github.tnoalex.processor.utils.filePath
import com.github.tnoalex.processor.utils.nameCanNotResolveWarn
import com.github.tnoalex.processor.utils.resolveToDescriptorIfAny
import com.github.tnoalex.processor.utils.startLine
import com.intellij.psi.PsiFile
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtObjectDeclaration
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtTreeVisitorVoid
import org.jetbrains.kotlin.resolve.descriptorUtil.isEffectivelyPublicApi
import org.jetbrains.kotlin.resolve.jvm.annotations.findJvmFieldAnnotation
import org.jetbrains.kotlin.utils.addToStdlib.ifFalse
import org.slf4j.LoggerFactory

@Component
@Suitable(LaunchEnvironment.CLI)
class NonJVMFieldCompanionValueProcessor : IssueProcessor {
    override val severity: Severity = Severity.SUGGESTION
    override val supportLanguage: List<Language> = listOf(JavaLanguage, KotlinLanguage)

    @EventListener(filterClazz = [KtFile::class])
    override fun process(psiFile: PsiFile) {
        psiFile.accept(companionObjectVisitor)
    }

    private val companionObjectVisitor = object : KtTreeVisitorVoid() {
        override fun visitObjectDeclaration(declaration: KtObjectDeclaration) {
            declaration.isCompanion().ifFalse { return super.visitObjectDeclaration(declaration) }
            declaration.accept(propertyVisitorVoid)
            super.visitObjectDeclaration(declaration)
        }
    }

    private val propertyVisitorVoid = object : KtTreeVisitorVoid() {
        override fun visitProperty(property: KtProperty) {
            if (property.hasDelegate()) return super.visitProperty(property)
            property.resolveToDescriptorIfAny()?.let {
                if (!it.isEffectivelyPublicApi) return super.visitProperty(property)
                if (it.isConst) return super.visitProperty(property)
                if (it.findJvmFieldAnnotation() != null) return super.visitProperty(property)
            } ?: return super.visitProperty(property)
            context.reportIssue(
                NonJVMFieldCompanionValueIssue(
                    property.filePath,
                    property.fqName?.asString() ?: let {
                        logger.nameCanNotResolveWarn("property", property)
                        "unknown property name"
                    },
                    property.text,
                    property.startLine
                )
            )
            super.visitProperty(property)
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(NonJVMFieldCompanionValueProcessor::class.java)
    }
}