package com.github.tnoalex.foundation.metrics

import com.github.tnoalex.foundation.astprocessor.kotlin.KotlinAstHook
import depends.extractor.kotlin.KotlinParser.*

class KotlinMccabeComplexityProcessor : MccabeComplexityProcessor() {
    override fun hookAst() {
        KotlinAstHook.hookEnterFunctionDeclaration { processFunctionDeclaration(it) }
        KotlinAstHook.hookEnterFunctionBody { processFunctionBody(it) }
        KotlinAstHook.hookEnterElvisExpression { processElvisExpression(it) }
        KotlinAstHook.hookEnterWhenExpression { processWhenExpression(it) }
        KotlinAstHook.hookEnterIfExpression { processIfExpression(it) }
        KotlinAstHook.hookEnterForStatement { processForStatement(it) }
        KotlinAstHook.hookEnterDoWhileStatement { processDoWhileStatement(it) }
        KotlinAstHook.hookEnterWhileStatement { processWhileStatement(it) }
        KotlinAstHook.hookEnterJumpExpression { processJumpExpression(it) }
        KotlinAstHook.hookEnterTryExpression { processTryExpression(it) }
    }

    private fun processFunctionDeclaration(ctx: FunctionDeclarationContext) {

    }

    private fun processFunctionBody(ctx: FunctionBodyContext) {

    }

    private fun processElvisExpression(ctx: ElvisExpressionContext) {

    }

    private fun processWhenExpression(ctx: WhenExpressionContext) {

    }

    private fun processIfExpression(ctx: IfExpressionContext) {

    }

    private fun processForStatement(ctx: ForStatementContext) {

    }

    private fun processDoWhileStatement(ctx: DoWhileStatementContext) {

    }

    private fun processWhileStatement(ctx: WhileStatementContext) {

    }

    private fun processJumpExpression(ctx: JumpExpressionContext) {

    }

    private fun processTryExpression(ctx: TryExpressionContext) {

    }

    override fun processFile(fullFileName: String) {

    }
}