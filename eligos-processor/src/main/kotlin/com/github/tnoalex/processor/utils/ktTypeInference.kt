package com.github.tnoalex.processor.utils

import org.jetbrains.kotlin.analysis.api.KaContextParameterApi
import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.descriptors.ConstructorDescriptor
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.VariableDescriptor
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.getChildrenOfType
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.types.typeUtil.supertypes

import org.jetbrains.kotlin.analysis.api.symbols.KaNamedClassSymbol
import org.jetbrains.kotlin.analysis.api.symbols.symbol

val KtElement.bindingContext
    get() = BindingContext.EMPTY

fun KtNamedFunction.resolveToDescriptorIfAny(): FunctionDescriptor? {
    return (this as KtDeclaration).resolveToDescriptorIfAny() as? FunctionDescriptor
}

fun KtProperty.resolveToDescriptorIfAny(): VariableDescriptor? {
    return (this as KtDeclaration).resolveToDescriptorIfAny() as? VariableDescriptor
}

fun KtConstructor<*>.resolveToDescriptorIfAny(): ConstructorDescriptor? {
    return (this as KtDeclaration).resolveToDescriptorIfAny() as? ConstructorDescriptor
}

fun KtDeclaration.resolveToDescriptorIfAny(): DeclarationDescriptor? {
    val context = this.bindingContext
    return if (this is KtParameter && hasValOrVar()) {
        context.get(BindingContext.PRIMARY_CONSTRUCTOR_PARAMETER, this)
            ?: context.get(BindingContext.DECLARATION_TO_DESCRIPTOR, this)
    } else {
        context.get(BindingContext.DECLARATION_TO_DESCRIPTOR, this)
    }
}

fun KtExpression.referenceExpressionSelfOrInChildren(): List<KtReferenceExpression> {
    val result = ArrayList<KtReferenceExpression>()
    if (this is KtReferenceExpression)
        result.add(this)
    result.addAll(this.getChildrenOfType<KtReferenceExpression>())
    return result
}

val KtClass.superTypes
    get() = this.bindingContext[BindingContext.CLASS, this]?.defaultType?.supertypes()

@OptIn(KaContextParameterApi::class)
context(_: KaSession)
val KtObjectDeclaration.superTypes
            get() =
                (symbol as KaNamedClassSymbol).superTypes