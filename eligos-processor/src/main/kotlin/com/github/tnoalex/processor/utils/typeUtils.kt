package com.github.tnoalex.processor.utils

import org.jetbrains.kotlin.cfg.containingDeclarationForPseudocode
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.calls.smartcasts.DataFlowValueFactory
import org.jetbrains.kotlin.resolve.calls.smartcasts.Nullability
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.util.javaslang.getOrNull

@Suppress("NOTHING_TO_INLINE")
inline fun getNullability(
    bindingContext: BindingContext, expression: KtExpression,
    dataFlowValueFactory: DataFlowValueFactory, type: KotlinType
): Nullability? {
    val typeInfo = bindingContext[BindingContext.EXPRESSION_TYPE_INFO, expression]
    val dataFlowInfo = typeInfo?.dataFlowInfo ?: return null
    val completeNullabilityInfo = dataFlowInfo.completeNullabilityInfo
    val declarationDescriptor =
        expression.containingDeclarationForPseudocode?.resolveToDescriptorIfAny()
            ?: return null
    val dataFlowValue =
        dataFlowValueFactory.createDataFlowValue(expression, type, bindingContext, declarationDescriptor)
    return completeNullabilityInfo[dataFlowValue].getOrNull()
}