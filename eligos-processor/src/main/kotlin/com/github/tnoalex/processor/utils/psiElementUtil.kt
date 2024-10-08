package com.github.tnoalex.processor.utils

import com.intellij.psi.PsiClass
import com.intellij.psi.PsiClassType
import com.intellij.psi.PsiElement
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

fun checkAnyRecursively(type: KotlinType, condition: (KotlinType) -> Boolean): Boolean {
    if (!type.constructor.isDenotable) {
        // for intersection type
        if (type.constructor.supertypes.any { checkAnyRecursively(it, condition) }) {
            return true
        }
    } else if (condition(type)) {
        return true
    }
    return type.arguments.any { (it !is StarProjectionImpl) && checkAnyRecursively(it.type, condition) }
}