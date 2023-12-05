package com.github.tnoalex.processor.kotlin

import com.github.tnoalex.foundation.eventbus.EventListener
import com.github.tnoalex.issues.OptimizedTailRecursionIssue
import com.github.tnoalex.processor.AstProcessorWithContext
import com.github.tnoalex.utils.independent
import com.github.tnoalex.utils.nearestCallSuffixExpression
import com.github.tnoalex.utils.paramsNum
import com.github.tnoalex.utils.signature
import depends.extractor.kotlin.KotlinParser.*
import depends.extractor.kotlin.KotlinParserBaseVisitor


class TailRecursionProcessor : AstProcessorWithContext() {
    override val supportLanguage: List<String>
        get() = listOf("kotlin")

    @EventListener(
        "!com.github.tnoalex.utils.KotlinAstUtilKt.isClosure(#{ctx}) && " +
                "com.github.tnoalex.utils.KotlinAstUtilKt.functionModifier(#{ctx}) != \"tailrec\""
    )
    fun process(ctx: FunctionDeclarationContext) {
        var isTailRec = false
        ctx.functionBody()?.let { isTailRec = findRecursion(it, ctx.simpleIdentifier().text, ctx.paramsNum()) }
        if (isTailRec) {
            val issue = OptimizedTailRecursionIssue(
                context.getLastElement().elementName!!,
                ctx.signature()
            )
            context.reportIssue(issue)
        }
    }

    private fun findRecursion(ctx: FunctionBodyContext, functionName: String, paramsNum: Int): Boolean {
        var independentFlag = true
        var isRecursion = false
        ctx.accept(object : KotlinParserBaseVisitor<Unit>() {
            override fun visitJumpExpression(ctx: JumpExpressionContext) {
                if (ctx.RETURN() == null) return
                if (!independentFlag) return
                val callSuffixContext = ctx.expression()?.nearestCallSuffixExpression() ?: return
                val postfixUnaryExpressionContext = (callSuffixContext.parent.parent as PostfixUnaryExpressionContext)

                val invokeParams = callSuffixContext.valueArguments().valueArgument().size
                val invokeName = postfixUnaryExpressionContext.postfixUnaryExpression().text

                if (invokeName == functionName && invokeParams == paramsNum) {
                    isRecursion = true
                    if (!postfixUnaryExpressionContext.independent()) {
                        independentFlag = false
                    }
                }
                super.visitJumpExpression(ctx)
            }
        })
        return independentFlag && isRecursion
    }
}