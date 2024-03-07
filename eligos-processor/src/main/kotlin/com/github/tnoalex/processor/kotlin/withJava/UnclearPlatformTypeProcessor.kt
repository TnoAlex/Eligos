package com.github.tnoalex.processor.kotlin.withJava

import com.github.tnoalex.foundation.LaunchEnvironment
import com.github.tnoalex.foundation.bean.Component
import com.github.tnoalex.foundation.bean.Suitable
import com.github.tnoalex.foundation.eventbus.EventListener
import com.github.tnoalex.issues.UnclearPlatformType
import com.github.tnoalex.processor.PsiProcessor
import com.github.tnoalex.processor.utils.resolveToDescriptorIfAny
import com.github.tnoalex.processor.utils.startLine
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtTreeVisitorVoid
import org.jetbrains.kotlin.types.lowerIfFlexible
import org.jetbrains.kotlin.types.upperIfFlexible

@Component
@Suitable(LaunchEnvironment.CLI)
class UnclearPlatformTypeProcessor : PsiProcessor {
    override val supportLanguage: List<String>
        get() = listOf("java", "kotlin")

    @EventListener
    fun process(ktFile: KtFile) {
        ktFile.accept(kotlinPropertyVisitor)
    }

    private val kotlinPropertyVisitor = object : KtTreeVisitorVoid() {
        override fun visitProperty(property: KtProperty) {
            val descriptor = property.resolveToDescriptorIfAny() ?: return
            val propertyType = descriptor.type
            if (propertyType.lowerIfFlexible() != propertyType.upperIfFlexible()) {
                //found platform type
                context.reportIssue(
                    UnclearPlatformType(
                        property.containingFile.virtualFile.path,
                        property.name ?: "unknown local property name",
                        property.startLine,
                        propertyType.upperIfFlexible().toString(),
                        propertyType.lowerIfFlexible().toString(),
                        property.isLocal,
                        property.isTopLevel,
                        property.isMember
                    )
                )
            }
            super.visitProperty(property)
        }
    }
}