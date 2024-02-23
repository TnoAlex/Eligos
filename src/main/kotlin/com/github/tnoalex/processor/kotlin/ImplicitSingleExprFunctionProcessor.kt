package com.github.tnoalex.processor.kotlin

import com.github.tnoalex.foundation.bean.Component
import com.github.tnoalex.foundation.eventbus.EventListener
import com.github.tnoalex.issues.ImplicitSingleExprFunctionIssue
import com.github.tnoalex.processor.PsiProcessorWithContext
import com.github.tnoalex.utils.signature
import depends.extractor.kotlin.KotlinParser.FunctionDeclarationContext

@Component
class ImplicitSingleExprFunctionProcessor : PsiProcessorWithContext() {
    override val supportLanguage: List<String>
        get() = listOf("kotlin")

    @EventListener(filter = "#{ctx}.functionBody().?ASSIGNMENT() != null", eventPrefix = "enter")
    fun process(ctx: FunctionDeclarationContext) {
        if (ctx.type() == null) {
            context.reportIssue(
                ImplicitSingleExprFunctionIssue(context.getLastElement().elementName!!, ctx.signature())
            )
        }
    }
}