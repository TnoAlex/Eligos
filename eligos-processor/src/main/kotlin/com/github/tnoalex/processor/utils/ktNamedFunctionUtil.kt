package com.github.tnoalex.processor.utils

import com.github.tnoalex.foundation.ApplicationContext
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtParameter
import org.jetbrains.kotlin.references.fe10.base.KtFe10ReferenceResolutionHelper
import org.jetbrains.kotlin.resolve.BindingContext

val KtElement.bindingContext
    get() = ApplicationContext.getBean(KtFe10ReferenceResolutionHelper::class.java).first().partialAnalyze(this)

fun KtNamedFunction.resolveToDescriptorIfAny(): FunctionDescriptor? {
    return (this as KtDeclaration).resolveToDescriptorIfAny() as? FunctionDescriptor
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