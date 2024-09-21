package com.github.tnoalex.processor.kotlin.withJava

import com.github.tnoalex.foundation.LaunchEnvironment
import com.github.tnoalex.foundation.bean.Component
import com.github.tnoalex.foundation.bean.Suitable
import com.github.tnoalex.foundation.eventbus.EventListener
import com.github.tnoalex.foundation.language.JavaLanguage
import com.github.tnoalex.foundation.language.KotlinLanguage
import com.github.tnoalex.foundation.language.Language
import com.github.tnoalex.issues.Severity
import com.github.tnoalex.issues.kotlin.withJava.JavaExtendOrImplInternalKotlinIssue
import com.github.tnoalex.issues.kotlin.withJava.JavaParameterInternalKotlinIssue
import com.github.tnoalex.issues.kotlin.withJava.JavaReturnInternalKotlinIssue
import com.github.tnoalex.processor.IssueProcessor
import com.github.tnoalex.processor.utils.filePath
import com.github.tnoalex.processor.utils.kotlinOriginCanNotResolveWarn
import com.github.tnoalex.processor.utils.nameCanNotResolveWarn
import com.github.tnoalex.processor.utils.startLine
import com.intellij.lang.jvm.JvmModifier
import com.intellij.psi.*
import com.intellij.psi.impl.source.PsiClassReferenceType
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.asJava.classes.KtLightClass
import org.jetbrains.kotlin.lexer.KtTokens
import org.slf4j.LoggerFactory

@Component
@Suitable(LaunchEnvironment.CLI)
class InternalExposedProcessor : IssueProcessor {
    override val severity: Severity
        get() = Severity.CODE_SMELL
    override val supportLanguage: List<Language>
        get() = listOf(JavaLanguage, KotlinLanguage)

    @EventListener(filterClazz = [PsiJavaFile::class])
    override fun process(psiFile: PsiFile) {
        psiFile.accept(javaClassVisitor)
    }

    private val javaClassVisitor = object : JavaRecursiveElementVisitor() {
        override fun visitMethod(method: PsiMethod?) {
            checkMethodReturnType(method ?: return)
            checkMethodParameters(method)
            super.visitMethod(method)
        }

        override fun visitClass(aClass: PsiClass?) {
            checkExtendOrImpl(aClass ?: return)
            super.visitClass(aClass)
        }

        fun checkMethodParameters(method: PsiMethod) {
            val parameters = method.parameters
            val exposedParameters = arrayListOf<Pair<Int, PsiClass>>()
            for ((index, param) in parameters.withIndex()) {
                val paramType = param.type
                if (paramType !is PsiClassReferenceType) continue
                val paramTypeClass = paramType.resolve() ?: continue
                if (paramTypeClass !is KtLightClass) continue
                paramTypeClass.kotlinOrigin ?: let {
                    logger.kotlinOriginCanNotResolveWarn("class", paramTypeClass)
                    return
                }
                val hasModifier = paramTypeClass.kotlinOrigin!!.hasModifier(KtTokens.INTERNAL_KEYWORD)
                if (hasModifier) {
                    exposedParameters.add(index to paramTypeClass)
                }
            }
            if (exposedParameters.isEmpty()) return
            context.reportIssue(
                JavaParameterInternalKotlinIssue(
                    hashSetOf(
                        method.containingFile?.virtualFile?.path ?: "unknown java file",
                    ).apply {
                        addAll(
                            exposedParameters.map {
                                it.second.containingFile?.virtualFile?.path ?: "unknown kotlin file"
                            }
                        )
                    },
                    method.containingClass?.qualifiedName ?: "anonymous java class name",
                    method.name,
                    method.startLine,
                    exposedParameters.map { it.first },
                    exposedParameters.map { it.second.qualifiedName ?: "anonymous kotlin class name" }
                )
            )
        }

        fun checkMethodReturnType(method: PsiMethod) {
            val returnType = method.returnType ?: return
            if (returnType !is PsiClassReferenceType) return
            val psiClass = returnType.resolve() ?: return
            if (psiClass !is KtLightClass) return
            psiClass.kotlinOrigin ?: let {
                logger.kotlinOriginCanNotResolveWarn("class", psiClass)
                return
            }
            val hasModifier = psiClass.kotlinOrigin!!.hasModifier(KtTokens.INTERNAL_KEYWORD)
            if (hasModifier) {
                context.reportIssue(
                    JavaReturnInternalKotlinIssue(
                        hashSetOf(
                            method.containingFile?.virtualFile?.path ?: "unknown java file",
                            psiClass.containingFile?.virtualFile?.path ?: "unknown kotlin file"
                        ),
                        method.containingClass?.qualifiedName ?: "anonymous java class name",
                        method.name,
                        method.startLine,
                        psiClass.qualifiedName ?: "anonymous kotlin class name"
                    )
                )
            }
        }

        fun checkExtendOrImpl(aClass: PsiClass) {
            if (aClass.superClass == null) return
            val superClass = aClass.superClass ?: return
            val interfaces = aClass.interfaces.filterIsInstance<KtLightClass>()
                .filter { it.kotlinOrigin != null && it.kotlinOrigin!!.hasModifier(KtTokens.INTERNAL_KEYWORD) }
            if (superClass !is KtLightClass && interfaces.isEmpty()) return
            if (superClass is KtLightClass) {
                superClass.kotlinOrigin ?: let {
                    logger.kotlinOriginCanNotResolveWarn("class", aClass)
                    return
                }
                if (!superClass.kotlinOrigin!!.hasModifier(KtTokens.INTERNAL_KEYWORD)) return
            }
            if (!isAllPublic(aClass)) return

            val filePaths = hashSetOf(aClass.containingFile.virtualFile.path)
            var superClassName: String? = null
            if (superClass is KtLightClass) {
                filePaths.add(superClass.containingFile.virtualFile.path)
                superClassName = superClass.kotlinOrigin?.fqName?.asString() ?: "unknown kotlin class"
            }

            context.reportIssue(
                JavaExtendOrImplInternalKotlinIssue(
                    (filePaths + interfaces.map {
                        it.kotlinOrigin?.filePath ?: let {
                            logger.kotlinOriginCanNotResolveWarn("class", aClass)
                            "unknown kotlin file"
                        }
                    }).toHashSet(),
                    aClass.qualifiedName ?: let {
                        logger.nameCanNotResolveWarn("class", aClass)
                        "unknown java class"
                    },
                    superClassName,
                    interfaces.map {
                        val name = it.kotlinOrigin?.fqName?.asString()
                        if (name == null) {
                            logger.nameCanNotResolveWarn("interface", it)
                            "unknown kotlin interface"
                        } else name
                    }.takeIf { it.isNotEmpty() }
                )
            )
        }
    }

    private fun isAllPublic(psiClass: PsiClass): Boolean {
        if (!psiClass.hasModifier(JvmModifier.PUBLIC)) return false
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