package com.github.tnoalex.processor.kotlin.withJava

import com.github.tnoalex.foundation.LaunchEnvironment
import com.github.tnoalex.foundation.bean.Component
import com.github.tnoalex.foundation.bean.Suitable
import com.github.tnoalex.foundation.eventbus.EventListener
import com.github.tnoalex.foundation.language.JavaLanguage
import com.github.tnoalex.foundation.language.KotlinLanguage
import com.github.tnoalex.foundation.language.Language
import com.github.tnoalex.issues.Severity
import com.github.tnoalex.issues.kotlin.withJava.optional.ParameterOptionalIssue
import com.github.tnoalex.issues.kotlin.withJava.optional.PropertyIsOptionalIssue
import com.github.tnoalex.issues.kotlin.withJava.optional.ReturnOptionalIssue
import com.github.tnoalex.processor.IssueProcessor
import com.github.tnoalex.processor.utils.*
import com.github.tnoalex.processor.utils.filePath
import com.github.tnoalex.processor.utils.typeCanNotResolveWarn
import com.intellij.psi.PsiFile
import org.jetbrains.kotlin.descriptors.ValueDescriptor
import org.jetbrains.kotlin.js.descriptorUtils.getKotlinTypeFqName
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.containingClass
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.isDynamic
import org.slf4j.LoggerFactory

@Component
@Suitable(LaunchEnvironment.CLI)
class OptionalInKotlinProcessor : IssueProcessor {
    override val severity: Severity
        get() = Severity.CODE_SMELL
    override val supportLanguage: List<Language>
        get() = listOf(JavaLanguage, KotlinLanguage)

    @EventListener(filterClazz = [KtFile::class])
    override fun process(psiFile: PsiFile) {
        psiFile.accept(visitor)
    }

    private val visitor = object : KtTreeVisitorVoid() {
        override fun visitProperty(property: KtProperty) {
            checkProperty(property)
            super.visitProperty(property)
        }

        override fun visitNamedFunction(function: KtNamedFunction) {
            checkFunction(function)
            super.visitNamedFunction(function)
        }

        private fun checkFunction(function: KtNamedFunction) {
            checkReturnType(function)
            checkParameters(function)
        }

        private fun checkReturnType(function: KtNamedFunction) {
            val returnType = function.resolveToDescriptorIfAny()?.returnType
                ?: let {
                    logger.typeCanNotResolveWarn("return", function)
                    return
                }
            if (checkAnyRecursively(returnType, ::isOptional)) {
                context.reportIssue(
                    ReturnOptionalIssue(
                        function.filePath,
                        function.containingClass()?.fqName?.asString() ?: "anonymous kotlin class",
                        function.name ?: "anonymous kotlin function",
                        function.startLine
                    )
                )
            }
        }

        private fun checkParameters(function: KtNamedFunction) {
            val valueParameters = function.valueParameters
            val optionalIndices = mutableListOf<Int>()
            for ((index, parameter) in valueParameters.withIndex()) {
                val descriptor = parameter.resolveToDescriptorIfAny() ?: continue
                if (descriptor is ValueDescriptor) {
                    if (checkAnyRecursively(descriptor.type, ::isOptional)) {
                        optionalIndices.add(index)
                    }
                }
            }
            if (optionalIndices.isNotEmpty()) {
                context.reportIssue(
                    ParameterOptionalIssue(
                        function.filePath,
                        function.containingClass()?.fqName?.asString() ?: "anonymous kotlin class",
                        function.name ?: "anonymous kotlin function",
                        function.startLine,
                        optionalIndices
                    )
                )
            }
        }

        private fun isOptional(kotlinType: KotlinType): Boolean {
            if (kotlinType.isDynamic()) return false
            // dynamic type can not be resolved
            val fqName = kotlinType.getKotlinTypeFqName(false)
            return fqName == OPTIONAL_NAME
        }

        private fun checkProperty(property: KtProperty) {
            val descriptor = property.resolveToDescriptorIfAny() ?: let {
                logger.typeCanNotResolveWarn("property", property)
                return
            }
            if (checkAnyRecursively(descriptor.type, ::isOptional)) {
                context.reportIssue(
                    PropertyIsOptionalIssue(
                        property.filePath,
                        property.name ?: "anonymous property",
                        property.startLine,
                        property.isTopLevel,
                        property.isLocal
                    )
                )
            }
        }
    }

    companion object {
        @JvmStatic
        private val logger = LoggerFactory.getLogger(UncertainNullablePlatformTypeProcessor::class.java)

        private const val OPTIONAL_NAME = "java.util.Optional"
    }
}