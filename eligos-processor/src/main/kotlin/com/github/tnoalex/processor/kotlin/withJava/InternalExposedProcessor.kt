package com.github.tnoalex.processor.kotlin.withJava

import com.github.tnoalex.foundation.LaunchEnvironment
import com.github.tnoalex.foundation.bean.Component
import com.github.tnoalex.foundation.bean.Suitable
import com.github.tnoalex.foundation.eventbus.EventListener
import com.github.tnoalex.foundation.language.JavaLanguage
import com.github.tnoalex.foundation.language.KotlinLanguage
import com.github.tnoalex.foundation.language.Language
import com.github.tnoalex.issues.Severity
import com.github.tnoalex.issues.kotlin.withJava.internalExpose.JavaExtendOrImplInternalKotlinIssue
import com.github.tnoalex.issues.kotlin.withJava.internalExpose.JavaParameterInternalKotlinIssue
import com.github.tnoalex.issues.kotlin.withJava.internalExpose.JavaReturnInternalKotlinIssue
import com.github.tnoalex.processor.IssueProcessor
import com.github.tnoalex.processor.utils.*
import com.github.tnoalex.processor.utils.filePath
import com.github.tnoalex.processor.utils.kotlinOriginCanNotResolveWarn
import com.github.tnoalex.processor.utils.nameCanNotResolveWarn
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
    override val severity: Severity = Severity.CODE_SMELL
    override val supportLanguage: List<Language> = listOf(JavaLanguage, KotlinLanguage)

    @EventListener(filterClazz = [PsiJavaFile::class])
    override fun process(psiFile: PsiFile) {
        psiFile.accept(javaClassVisitor)
    }

    private val javaClassVisitor = object : JavaRecursiveElementVisitor() {
        override fun visitMethod(method: PsiMethod?) {
            if (method == null) return
            if (!isAllPublic(method)) return
            checkMethodReturnType(method)
            checkMethodParameters(method)
            super.visitMethod(method)
        }

        override fun visitClass(aClass: PsiClass?) {
            checkExtendOrImpl(aClass ?: return)
            super.visitClass(aClass)
        }

        /**
         * check if a [PsiClass] which resolved **from [PsiClassReferenceType]** is a Kotlin internal class.
         */
        fun isKtInternal(psiClass: PsiClass): Boolean {
            if (psiClass !is KtLightClass) return false
            psiClass.kotlinOrigin ?: let {
                logger.kotlinOriginCanNotResolveWarn("class", psiClass)
                return false
            }
            return psiClass.kotlinOrigin!!.hasModifier(KtTokens.INTERNAL_KEYWORD)
        }


        fun checkMethodParameters(method: PsiMethod) {
            val parameters = method.parameters
            val exposedParameters = arrayListOf<Pair<Int, List<PsiClass>>>()
            for ((index, param) in parameters.withIndex()) {
                val paramType = param.type
                val exposedTypes = mutableListOf<PsiClass>()
                if (paramType is PsiClassType) {
                    collectRecursively(paramType, exposedTypes, ::isKtInternal)
                }
                if (exposedTypes.isNotEmpty()) {
                    exposedParameters.add(index to exposedTypes)
                }
            }
            if (exposedParameters.isEmpty()) return
            context.reportIssue(
                JavaParameterInternalKotlinIssue(
                    hashSetOf(
                        method.containingFile?.virtualFile?.path ?: "unknown java file",
                    ).apply {
                        addAll(
                            exposedParameters.flatMap {
                                it.second.map { it1 ->
                                    it1.containingFile?.virtualFile?.path ?: "unknown kotlin file"
                                }
                            }
                        )
                    },
                    method.containingClass?.qualifiedName ?: "anonymous java class name",
                    method.name,
                    method.startLine,
                    exposedParameters.map { it.first },
                    exposedParameters.map {
                        it.second.map { it1 ->
                            it1.qualifiedName ?: "anonymous kotlin class name"
                        }.toSet()
                    }
                )
            )
        }

        fun checkMethodReturnType(method: PsiMethod) {
            val returnType = method.returnType ?: return
            if (returnType !is PsiClassType) return
            val exposedTypes = mutableListOf<PsiClass>()
            collectRecursively(returnType, exposedTypes, ::isKtInternal)
            if (exposedTypes.isNotEmpty()) {
                context.reportIssue(
                    JavaReturnInternalKotlinIssue(
                        hashSetOf(
                            method.containingFile?.virtualFile?.path ?: "unknown java file"
                        ).apply {
                            addAll(
                                exposedTypes.map {
                                    it.containingFile?.virtualFile?.path ?: "unknown kotlin file"
                                }
                            )
                        },
                        method.containingClass?.qualifiedName ?: "anonymous java class name",
                        method.name,
                        method.startLine,
                        exposedTypes.map {
                            it.qualifiedName ?: "anonymous kotlin class name"
                        }.toSet()
                    )
                )
            }
        }

        fun checkExtendOrImpl(aClass: PsiClass) {
            if (!isAllPublic(aClass)) return
            val exposedTypes = mutableListOf<PsiClass>()
            val superTypes = aClass.superTypes
            for (type in superTypes) {
                collectRecursively(type, exposedTypes, ::isKtInternal)
            }
            val interfaces = aClass.implementsListTypes
            for (type in interfaces) {
                collectRecursively(type, exposedTypes, ::isKtInternal)
            }
            if (exposedTypes.isNotEmpty()) {
                context.reportIssue(
                    JavaExtendOrImplInternalKotlinIssue(
                        hashSetOf(aClass.filePath).apply {
                            addAll(exposedTypes.map { it.filePath })
                        },
                        aClass.qualifiedName ?: let {
                            logger.nameCanNotResolveWarn("class", aClass)
                            "unknown java class"
                        },
                        exposedTypes.map { it.qualifiedName ?: "anonymous kotlin class" }.toHashSet()
                    )
                )
            }
        }
    }

    private fun isAllPublic(element: PsiModifierListOwner): Boolean {
        if (!element.hasModifier(JvmModifier.PUBLIC)) return false
        val parent = PsiTreeUtil.getParentOfType(element, PsiClass::class.java)
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