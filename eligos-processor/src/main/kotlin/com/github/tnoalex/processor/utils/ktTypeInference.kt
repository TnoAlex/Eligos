package com.github.tnoalex.processor.utils

import org.jetbrains.kotlin.analysis.api.KaContextParameterApi
import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.symbols.symbol
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtObjectDeclaration
import org.jetbrains.kotlin.psi.KtReferenceExpression
import org.jetbrains.kotlin.psi.psiUtil.getChildrenOfType

fun KtExpression.referenceExpressionSelfOrInChildren(): List<KtReferenceExpression> {
    val result = ArrayList<KtReferenceExpression>()
    if (this is KtReferenceExpression)
        result.add(this)
    result.addAll(this.getChildrenOfType<KtReferenceExpression>())
    return result
}

@OptIn(KaContextParameterApi::class)
context(_: KaSession)
val KtObjectDeclaration.superTypes
    get() = symbol.superTypes