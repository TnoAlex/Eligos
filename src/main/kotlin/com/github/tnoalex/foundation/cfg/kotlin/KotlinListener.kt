package com.github.tnoalex.foundation.cfg.kotlin

import com.github.tnoalex.utils.*
import depends.extractor.kotlin.KotlinParser
import depends.extractor.kotlin.KotlinParserBaseListener

class KotlinListener(
    private val controlFlowBuilder: KotlinControlFlowBuilder
) : KotlinParserBaseListener() {
    override fun enterFunctionDeclaration(ctx: KotlinParser.FunctionDeclarationContext) {
        val currentFunction = getCurrentFunction(ctx)
        controlFlowBuilder.enteredFunction(
            ctx.simpleIdentifier().text,
            getCurrentClass(ctx)?.simpleIdentifier()?.text,
            currentFunction?.simpleIdentifier()?.text,
            currentFunction?.paramsNum(),
            ctx.visibilityModifier() ?: "public",
            ctx.functionModifier(),
            ctx.inheritanceModifier(),
            ctx.paramsNum()
        )
        super.enterFunctionDeclaration(ctx)
    }

    override fun enterFunctionBody(ctx: KotlinParser.FunctionBodyContext?) {
        ctx?.block()?.statements()?.statement()?.forEach {
            if (it.loopStatement() != null) {

            } else if (it.expression() != null) {
                it.expression()
            }
        }
        super.enterFunctionBody(ctx)
    }

    override fun exitFunctionBody(ctx: KotlinParser.FunctionBodyContext?) {
        super.exitFunctionBody(ctx)
    }
}