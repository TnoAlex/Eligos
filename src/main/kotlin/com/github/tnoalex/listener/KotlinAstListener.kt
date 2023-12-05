package com.github.tnoalex.listener

import com.github.tnoalex.foundation.eventbus.EventBus
import depends.extractor.kotlin.KotlinParser
import depends.extractor.kotlin.KotlinParserBaseListener

class KotlinAstListener : AstListener, KotlinParserBaseListener() {
    override val supportLanguage: List<String>
        get() = listOf("kotlin")

    override fun enterKotlinFile(ctx: KotlinParser.KotlinFileContext) {
        EventBus.post(ctx)
        super.enterKotlinFile(ctx)
    }

    override fun enterFunctionDeclaration(ctx: KotlinParser.FunctionDeclarationContext) {
        EventBus.post(ctx)
        super.enterFunctionDeclaration(ctx)
    }

    override fun exitFunctionDeclaration(ctx: KotlinParser.FunctionDeclarationContext) {
        EventBus.post(ctx)
        super.exitFunctionDeclaration(ctx)
    }

    override fun enterFunctionBody(ctx: KotlinParser.FunctionBodyContext) {
        EventBus.post(ctx)
        super.enterFunctionBody(ctx)
    }

    override fun exitFunctionBody(ctx: KotlinParser.FunctionBodyContext) {
        EventBus.post(ctx)
        super.exitFunctionBody(ctx)
    }

    override fun enterAssignment(ctx: KotlinParser.AssignmentContext) {
        EventBus.post(ctx)
        super.enterAssignment(ctx)
    }

    override fun enterPropertyDeclaration(ctx: KotlinParser.PropertyDeclarationContext) {
        EventBus.post(ctx)
        super.enterPropertyDeclaration(ctx)
    }

    override fun enterElvisExpression(ctx: KotlinParser.ElvisExpressionContext) {
        EventBus.post(ctx)
        super.enterElvisExpression(ctx)
    }

    override fun enterWhenExpression(ctx: KotlinParser.WhenExpressionContext) {
        EventBus.post(ctx)
        super.enterWhenExpression(ctx)
    }

    override fun enterIfExpression(ctx: KotlinParser.IfExpressionContext) {
        EventBus.post(ctx)
        super.enterIfExpression(ctx)
    }

    override fun enterForStatement(ctx: KotlinParser.ForStatementContext) {
        EventBus.post(ctx)
        super.enterForStatement(ctx)
    }

    override fun enterDoWhileStatement(ctx: KotlinParser.DoWhileStatementContext) {
        EventBus.post(ctx)
        super.enterDoWhileStatement(ctx)
    }

    override fun enterWhileStatement(ctx: KotlinParser.WhileStatementContext) {
        EventBus.post(ctx)
        super.enterWhileStatement(ctx)
    }

    override fun enterJumpExpression(ctx: KotlinParser.JumpExpressionContext) {
        EventBus.post(ctx)
        super.enterJumpExpression(ctx)
    }

    override fun enterTryExpression(ctx: KotlinParser.TryExpressionContext) {
        EventBus.post(ctx)
        super.enterTryExpression(ctx)
    }

    override fun enterExpression(ctx: KotlinParser.ExpressionContext) {
        EventBus.post(ctx)
        super.enterExpression(ctx)
    }
}