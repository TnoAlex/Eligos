package com.github.tnoalex.processor.utils

import com.intellij.psi.PsiClass
import com.intellij.psi.PsiClassType
import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.analysis.api.KaContextParameterApi
import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.components.isDenotable
import org.jetbrains.kotlin.analysis.api.types.KaClassType
import org.jetbrains.kotlin.analysis.api.types.KaType
import org.jetbrains.kotlin.types.*

internal val PsiElement.filePath
    get() = this.containingFile.virtualFile.path

fun collectRecursively(type: PsiClassType, result: MutableList<PsiClass>, condition: (PsiClass) -> Boolean) {
    val typeClass = type.resolve()
    if (typeClass != null && condition(typeClass)) {
        result.add(typeClass)
    }
    for (typeParamInClass in type.parameters) {
        if (typeParamInClass is PsiClassType) {
            collectRecursively(typeParamInClass, result, condition)
        }
    }
}

fun collectRecursively(type: KotlinType, result: MutableList<KotlinType>, condition: (KotlinType) -> Boolean) {
    if (condition(type)) {
        result.add(type)
    }
    for (argument in type.arguments) {
        collectRecursively(argument.type, result, condition)
    }
}

fun KaSession.checkAnyRecursively(type: KaType, condition: (KaType) -> Boolean): Boolean {
    if (!type.isDenotable) {
        // for intersection type
        if (type.allSupertypes.any { checkAnyRecursively(it, condition) }) {
            return true
        }
    } else if (type is KaClassType) {
        return type.typeArguments.any {
            val typeArg = it.type
            typeArg != null && checkAnyRecursively(typeArg, condition)
        }
    }
    return condition(type)
}