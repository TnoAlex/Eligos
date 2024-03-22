package com.github.tnoalex.processor.kotlin.withJava

import com.github.tnoalex.foundation.LaunchEnvironment
import com.github.tnoalex.foundation.bean.Component
import com.github.tnoalex.foundation.bean.Suitable
import com.github.tnoalex.foundation.eventbus.EventListener
import com.github.tnoalex.issues.kotlin.withJava.InternalExposedIssue
import com.github.tnoalex.processor.PsiProcessor
import com.intellij.lang.jvm.JvmModifier
import com.intellij.psi.JavaRecursiveElementVisitor
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.asJava.classes.KtLightClass
import org.jetbrains.kotlin.lexer.KtTokens
import org.slf4j.LoggerFactory

@Component
@Suitable(LaunchEnvironment.CLI)
class InternalExposedProcessor : PsiProcessor {
    override val supportLanguage: List<String>
        get() = listOf("kotlin", "java")

    @EventListener
    fun process(psiFile: PsiJavaFile) {
        psiFile.accept(javaClassVisitor)
    }

    private val javaClassVisitor = object : JavaRecursiveElementVisitor() {
        override fun visitClass(aClass: PsiClass) {
            if (aClass.superClass == null) return super.visitClass(aClass)
            val superClass = aClass.superClass ?: return super.visitClass(aClass)
            val interfaces = aClass.interfaces.filterIsInstance<KtLightClass>()
                .filter { it.kotlinOrigin != null && it.kotlinOrigin!!.hasModifier(KtTokens.INTERNAL_KEYWORD) }
            if (superClass !is KtLightClass && interfaces.isEmpty()) return super.visitClass(aClass)
            if (superClass is KtLightClass) {
                superClass.kotlinOrigin ?: let {
                    logger.warn("Unknown kotlin source file during visit ${aClass.containingFile.name}")
                    return super.visitClass(aClass)
                }
                if (!superClass.kotlinOrigin!!.hasModifier(KtTokens.INTERNAL_KEYWORD)) return super.visitClass(aClass)
            }
            if (!aClass.hasModifier(JvmModifier.PUBLIC)) return super.visitClass(aClass)
            if (!isAllPublic(aClass)) return super.visitClass(aClass)

            val filePaths = hashSetOf(aClass.containingFile.virtualFile.path)
            var superClassName: String? = null
            if (superClass is KtLightClass) {
                filePaths.add(superClass.containingFile.virtualFile.path)
                superClassName = superClass.kotlinOrigin?.fqName?.asString() ?: "unknown kotlin class"
            }

            context.reportIssue(
                InternalExposedIssue(
                    (filePaths + interfaces.map {
                        it.kotlinOrigin?.containingFile?.virtualFile?.path ?: let {
                            logger.warn("Unknown kotlin source file during visit ${aClass.containingFile.name}")
                            "unknown kotlin file"
                        }
                    }).toHashSet(),
                    aClass.qualifiedName ?: let{
                        logger.warn("Unknown java class name during visit ${aClass.containingFile.name}")
                        "unknown java class"},
                    superClassName,
                    interfaces.map { it.kotlinOrigin?.fqName?.asString() ?: let{
                        logger.warn("Unknown kotlin interface name during visit ${aClass.containingFile.name}")
                        "unknown kotlin interface"}
                    }
                )
            )
            super.visitClass(aClass)
        }
    }

    private fun isAllPublic(psiClass: PsiClass): Boolean {
        val parent = PsiTreeUtil.getParentOfType(psiClass, PsiClass::class.java)
        return if (parent == null) true
        else {
            parent.hasModifier(JvmModifier.PUBLIC) && isAllPublic(parent)
        }
    }

    companion object {
        @JvmStatic
        private val logger = LoggerFactory.getLogger(InternalExposedProcessor::class.java)
    }
}