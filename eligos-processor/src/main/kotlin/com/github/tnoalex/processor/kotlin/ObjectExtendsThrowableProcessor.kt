package com.github.tnoalex.processor.kotlin

import com.github.tnoalex.foundation.LaunchEnvironment
import com.github.tnoalex.foundation.bean.Component
import com.github.tnoalex.foundation.bean.Suitable
import com.github.tnoalex.foundation.eventbus.EventListener
import com.github.tnoalex.issues.Severity
import com.github.tnoalex.issues.kotlin.ObjectExtendsThrowableIssue
import com.github.tnoalex.processor.PsiProcessor
import com.github.tnoalex.processor.utils.filePath
import com.github.tnoalex.processor.utils.nameCanNotResolveWarn
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
    override val severity: Severity
        get() = Severity.CODE_SMELL
    override val supportLanguage: List<String>
        get() = listOf("kotlin")

    @EventListener
    fun process(ktFile: KtFile) {
        ktFile.accept(objectVisitor)
    }

    private val objectVisitor = object : KtTreeVisitorVoid() {

        override fun visitObjectDeclaration(declaration: KtObjectDeclaration) {
            if (declaration.isCompanion()) return  super.visitObjectDeclaration(declaration)

            declaration.superTypes?.any { it.isNotNullThrowable() }?.ifTrue {
                context.reportIssue(ObjectExtendsThrowableIssue(
                    declaration.filePath,
                    declaration.fqName?.asString() ?: let {
                        logger.nameCanNotResolveWarn("object",declaration)
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