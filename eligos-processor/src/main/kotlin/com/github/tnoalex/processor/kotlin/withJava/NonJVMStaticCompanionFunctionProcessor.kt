package com.github.tnoalex.processor.kotlin.withJava

import com.github.tnoalex.foundation.LaunchEnvironment
import com.github.tnoalex.foundation.bean.Component
import com.github.tnoalex.foundation.bean.Suitable
import com.github.tnoalex.foundation.eventbus.EventListener
import com.github.tnoalex.issues.kotlin.withJava.NonJVMStaticCompanionFunctionIssue
import com.github.tnoalex.processor.PsiProcessor
import com.github.tnoalex.processor.utils.filePath
import com.github.tnoalex.processor.utils.nameCanNotResolveWarn
import com.github.tnoalex.processor.utils.resolveToDescriptorIfAny
import com.github.tnoalex.processor.utils.startLine
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtObjectDeclaration
import org.jetbrains.kotlin.psi.KtTreeVisitorVoid
import org.jetbrains.kotlin.resolve.descriptorUtil.isEffectivelyPublicApi
import org.jetbrains.kotlin.utils.addToStdlib.ifFalse
import org.slf4j.LoggerFactory

@Component
@Suitable(LaunchEnvironment.CLI)
class NonJVMStaticCompanionFunctionProcessor : PsiProcessor {
    override val supportLanguage: List<String>
        get() = listOf("kotlin", "java")

    @EventListener
    fun process(ktFile: KtFile) {
        ktFile.accept(companionObjectVisitor)
    }

    private val companionObjectVisitor = object : KtTreeVisitorVoid() {
        override fun visitObjectDeclaration(declaration: KtObjectDeclaration) {
            declaration.isCompanion().ifFalse { return super.visitObjectDeclaration(declaration) }
            declaration.accept(namedFunctionVisitorVoid)
            super.visitObjectDeclaration(declaration)
        }
    }

    private val namedFunctionVisitorVoid = object : KtTreeVisitorVoid() {
        override fun visitNamedFunction(function: KtNamedFunction) {
            function.resolveToDescriptorIfAny()?.let {
                if (!it.isEffectivelyPublicApi) return super.visitNamedFunction(function)
                if (it.annotations.findAnnotation(FqName(JVM_STATIC_FQ_NAME)) != null)
                    return super.visitNamedFunction(function)
            } ?: return super.visitNamedFunction(function)
            context.reportIssue(
                NonJVMStaticCompanionFunctionIssue(
                    function.filePath,
                    function.fqName?.asString() ?: let {
                        logger.nameCanNotResolveWarn("function", function)
                        "unknown function name"
                    },
                    function.valueParameters.map {
                        if (it.fqName != null) it.fqName!!.asString()
                        else {
                            logger.nameCanNotResolveWarn("parameter", it)
                            "unknown parameter name"
                        }
                    },
                    function.startLine,
                    function.text
                )
            )
            super.visitNamedFunction(function)
        }
    }

    companion object {
        private const val JVM_STATIC_FQ_NAME = "kotlin.jvm.JvmStatic"
        private val logger = LoggerFactory.getLogger(NonJVMStaticCompanionFunctionProcessor::class.java)
    }
}