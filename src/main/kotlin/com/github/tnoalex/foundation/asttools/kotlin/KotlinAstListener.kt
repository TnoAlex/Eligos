package com.github.tnoalex.foundation.asttools.kotlin

import depends.extractor.kotlin.KotlinParser
import depends.extractor.kotlin.KotlinParserBaseListener

class KotlinAstListener : KotlinParserBaseListener() {
    override fun enterFunctionDeclaration(ctx: KotlinParser.FunctionDeclarationContext) {
        KotlinAstHook.getHook(this::enterFunctionDeclaration.name).forEach {
            it(ctx)
        }
        super.enterFunctionDeclaration(ctx)
    }

    override fun exitFunctionDeclaration(ctx: KotlinParser.FunctionDeclarationContext) {
        KotlinAstHook.getHook(this::exitFunctionDeclaration.name).forEach {
            it(ctx)
        }
        super.exitFunctionDeclaration(ctx)
    }

    override fun enterFunctionBody(ctx: KotlinParser.FunctionBodyContext) {
        KotlinAstHook.getHook(this::enterFunctionBody.name).forEach {
            it(ctx)
        }
        super.enterFunctionBody(ctx)
    }

    override fun exitFunctionBody(ctx: KotlinParser.FunctionBodyContext) {
        KotlinAstHook.getHook(this::exitFunctionBody.name).forEach {
            it(ctx)
        }
        super.exitFunctionBody(ctx)
    }

    override fun enterAssignment(ctx: KotlinParser.AssignmentContext) {
        KotlinAstHook.getHook(this::enterAssignment.name).forEach {
            it(ctx)
        }
        super.enterAssignment(ctx)
    }

    override fun enterPropertyDeclaration(ctx: KotlinParser.PropertyDeclarationContext) {
        KotlinAstHook.getHook(this::enterPropertyDeclaration.name).forEach {
            it(ctx)
        }
        super.enterPropertyDeclaration(ctx)
    }

    override fun enterElvisExpression(ctx: KotlinParser.ElvisExpressionContext) {
        KotlinAstHook.getHook(this::enterElvisExpression.name).forEach {
            it(ctx)
        }
        super.enterElvisExpression(ctx)
    }

    override fun enterWhenExpression(ctx: KotlinParser.WhenExpressionContext) {
        KotlinAstHook.getHook(this::enterWhenExpression.name).forEach {
            it(ctx)
        }
        super.enterWhenExpression(ctx)
    }

    override fun enterIfExpression(ctx: KotlinParser.IfExpressionContext) {
        KotlinAstHook.getHook(this::enterIfExpression.name).forEach {
            it(ctx)
        }
        super.enterIfExpression(ctx)
    }

    override fun enterForStatement(ctx: KotlinParser.ForStatementContext) {
        KotlinAstHook.getHook(this::enterForStatement.name).forEach {
            it(ctx)
        }
        super.enterForStatement(ctx)
    }

    override fun enterDoWhileStatement(ctx: KotlinParser.DoWhileStatementContext) {
        KotlinAstHook.getHook(this::enterDoWhileStatement.name).forEach {
            it(ctx)
        }
        super.enterDoWhileStatement(ctx)
    }

    override fun enterWhileStatement(ctx: KotlinParser.WhileStatementContext) {
        KotlinAstHook.getHook(this::enterWhileStatement.name).forEach {
            it(ctx)
        }
        super.enterWhileStatement(ctx)
    }

    override fun enterJumpExpression(ctx: KotlinParser.JumpExpressionContext) {
        KotlinAstHook.getHook(this::enterJumpExpression.name).forEach {
            it(ctx)
        }
        super.enterJumpExpression(ctx)
    }

    override fun enterTryExpression(ctx: KotlinParser.TryExpressionContext) {
        KotlinAstHook.getHook(this::enterTryExpression.name).forEach {
            it(ctx)
        }
        super.enterTryExpression(ctx)
    }

    override fun enterExpression(ctx: KotlinParser.ExpressionContext) {
        KotlinAstHook.getHook(this::enterExpression.name).forEach {
            it(ctx)
        }
        super.enterExpression(ctx)
    }
}