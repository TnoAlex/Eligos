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

        fun checkExposedType(type: PsiClassType, result: MutableList<PsiClass>) {
            val typeClass = type.resolve()
            if (typeClass != null && isKtInternal(typeClass)) {
                result.add(typeClass)
            }
            for (typeParamInClass in type.parameters) {
                if (typeParamInClass is PsiClassType) {
                    checkExposedType(typeParamInClass, result)
                }
            }
        }

        fun checkMethodParameters(method: PsiMethod) {
            val parameters = method.parameters
            val exposedParameters = arrayListOf<Pair<Int, List<PsiClass>>>()
            for ((index, param) in parameters.withIndex()) {
                val paramType = param.type
                val exposedTypes = mutableListOf<PsiClass>()
                if (paramType is PsiClassType) {
                    checkExposedType(paramType, exposedTypes)
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
            checkExposedType(returnType, exposedTypes)
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