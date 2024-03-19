package com.github.tnoalex.processor.kotlin

import com.github.tnoalex.foundation.LaunchEnvironment
import com.github.tnoalex.foundation.bean.Component
import com.github.tnoalex.foundation.bean.Suitable
import com.github.tnoalex.foundation.eventbus.EventListener
import com.github.tnoalex.issues.kotlin.ObjectExtendsThrowableIssue
import com.github.tnoalex.processor.PsiProcessor
import com.github.tnoalex.processor.utils.startLine
import com.github.tnoalex.processor.utils.superTypes
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtObjectDeclaration
import org.jetbrains.kotlin.psi.KtTreeVisitorVoid
import org.jetbrains.kotlin.types.typeUtil.isNotNullThrowable
import org.jetbrains.kotlin.utils.addToStdlib.ifTrue
import org.slf4j.LoggerFactory

@Component
@Suitable(LaunchEnvironment.CLI)
class ObjectExtendsThrowableProcessor : PsiProcessor {
    override val supportLanguage: List<String>
        get() = listOf("kotlin")

    @EventListener
    fun process(ktFile: KtFile) {
        ktFile.accept(objectVisitor)
    }

    private val objectVisitor = object : KtTreeVisitorVoid() {

        override fun visitObjectDeclaration(declaration: KtObjectDeclaration) {
            if (declaration.isCompanion()) return

            declaration.superTypes?.any { it.isNotNullThrowable() }?.ifTrue {
                context.reportIssue(ObjectExtendsThrowableIssue(
                    declaration.containingKtFile.virtualFilePath,
                    declaration.fqName?.asString() ?: let {
                        logger.warn(
                            "Can not resolve object fq name in file ${declaration.containingKtFile.virtualFilePath}," +
                                    "line ${declaration.startLine}"
                        )
                        "unknown object name"
                    }
                )
                )
            }
            super.visitObjectDeclaration(declaration)
        }
    }

    companion object {
        @JvmStatic
        private val logger = LoggerFactory.getLogger(ObjectExtendsThrowableProcessor::class.java)
    }
}