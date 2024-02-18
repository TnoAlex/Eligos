package com.github.tnoalex.processor.kotlin

import com.github.tnoalex.foundation.bean.Component
import com.github.tnoalex.foundation.eventbus.EventListener
import com.github.tnoalex.issues.OptimizedTailRecursionIssue
import com.github.tnoalex.processor.AstProcessorWithContext
import com.github.tnoalex.utils.*
import depends.extractor.kotlin.KotlinParser.*
import depends.extractor.kotlin.KotlinParserBaseVisitor

@Component
class TailRecursionProcessor : AstProcessorWithContext() {
    override val order: Int
        get() = Int.MAX_VALUE
    override val supportLanguage: List<String>
        get() = listOf("kotlin")

    @EventListener("!com.github.tnoalex.utils.KotlinAstUtilKt.isClosure(#{ctx})", "enter")
    fun process(ctx: FunctionDeclarationContext) {
        if (ctx.modifiers()?.functionModifier() == "tailrec") return
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

                callSuffixContext.valueArguments() ?: return
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