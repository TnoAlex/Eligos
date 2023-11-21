package com.github.tnoalex.foundation.metrics

import com.github.tnoalex.foundation.asttools.hook.KotlinAstHook
import com.github.tnoalex.utils.*
import depends.extractor.kotlin.KotlinParser.*

class KotlinMccabeComplexityProcessor : MccabeComplexityProcessor() {
    override val supportLanguage: List<String>
        get() = listOf("kotlin")

    override fun hookAst() {
        KotlinAstHook.hookEnterFunctionDeclaration({ processFunctionDeclaration(it) }, this)
        KotlinAstHook.hookEnterElvisExpression({ processElvisExpression(it) }, this)
        KotlinAstHook.hookEnterWhenExpression({ processWhenExpression(it) }, this)
        KotlinAstHook.hookEnterIfExpression({ processIfExpression(it) }, this)
        KotlinAstHook.hookEnterForStatement({ processForStatement(it) }, this)
        KotlinAstHook.hookEnterDoWhileStatement({ processDoWhileStatement(it) }, this)
        KotlinAstHook.hookEnterWhileStatement({ processWhileStatement(it) }, this)
        KotlinAstHook.hookEnterJumpExpression({ processJumpExpression(it) }, this)
        KotlinAstHook.hookEnterTryExpression({ processTryExpression(it) }, this)
        KotlinAstHook.hookEnterAssignment({ processAssignment(it) }, this)
        KotlinAstHook.hookEnterPropertyDeclaration({ processPropertyDeclaration(it) }, this)
        KotlinAstHook.hookEnterExpression({ processExpression(it) }, this)
        KotlinAstHook.hookExitFunctionBody({ processExitFunction(it) }, this)
    }

    override fun removeHooks() {
        KotlinAstHook.removeHook(this)
    }

    private fun processFunctionDeclaration(ctx: FunctionDeclarationContext) {
        val parentFunc = getParentFunction(ctx) // determine whether it is a closure function
        val id = ctx.id()
        addFunction(id)
        addArc(id)
        addNode(id)
        if (parentFunc != null) {
            recordClosurePair(parentFunc.id(), id)
        }
    }

    private fun processExitFunction(ctx: FunctionBodyContext) {
        val parentFunc = getParentFunction(ctx)
            ?: throw RuntimeException("Unintended structures, which should not be function exports here")
        val id = parentFunc.id()
        addTerminatedNode(id)
    }

    private fun processElvisExpression(ctx: ElvisExpressionContext) {
        if (ctx.children.size == 1) return
        val parentFunc = getParentFunction(ctx) ?: return // top level property definition or assignment
        val id = parentFunc.id()
        addNode(id)
        addArc(id, 2)
    }

    private fun processAssignment(ctx: AssignmentContext) {
        if (ctx.children.size == 1) return
        val parentFunc = getParentFunction(ctx) ?: return //top level
        val id = parentFunc.id()
        addArc(id)
        addNode(id)
    }

    private fun processPropertyDeclaration(ctx: PropertyDeclarationContext) {
        if (ctx.children.size == 1) return
        val parentFunc = getParentFunction(ctx) ?: return
        val id = parentFunc.id()
        addArc(id)
        addNode(id)
    }

    private fun processExpression(ctx: ExpressionContext) {
        val parentFunc = getParentFunction(ctx) ?: return
        if (ctx.children.size == 1) return
        val id = parentFunc.id()
        if (ctx.ifExpression() != null) return
        else if (ctx.elvisExpression() != null) return
        else if (ctx.tryExpression() != null) return
        else if (ctx.jumpExpression() != null) return // other hook process
        addArc(id)
        addNode(id)
    }

    private fun processWhenExpression(ctx: WhenExpressionContext) {
        if (ctx.children.size == 1) return
        val parentFunc = getParentFunction(ctx)
            ?: throw RuntimeException("Syntax error, when expression are not allowed to appear at the top level")
        val id = parentFunc.id()
        addNode(id)
        val entrySize = ctx.whenEntry().size
        addArc(id, entrySize * 2)
        addNode(id, entrySize)
    }

    private fun processIfExpression(ctx: IfExpressionContext) {
        if (ctx.children.size == 1) return
        val parentFunc = getParentFunction(ctx) ?: return
        val id = parentFunc.id()
        addNode(id)
        addArc(id, 2)
    }

    private fun processForStatement(ctx: ForStatementContext) {
        val parentFunc = getParentFunction(ctx)
            ?: throw RuntimeException("Syntax error, for statements are not allowed to appear at the top level")
        val id = parentFunc.id()
        addNode(id)
        addArc(id, 2)
    }

    private fun processDoWhileStatement(ctx: DoWhileStatementContext) {
        val parentFunc = getParentFunction(ctx)
            ?: throw RuntimeException("Syntax error, do-while statements are not allowed to appear at the top level")
        val id = parentFunc.id()
        addNode(id)
        addArc(id, 2)
    }

    private fun processWhileStatement(ctx: WhileStatementContext) {
        val parentFunc = getParentFunction(ctx)
            ?: throw RuntimeException("Syntax error, while statements are not allowed to appear at the top level")
        val id = parentFunc.id()
        addNode(id)
        addArc(id, 2)
    }

    private fun processJumpExpression(ctx: JumpExpressionContext) {
        if (ctx.children.size == 1) return
        val parentFunc = getParentFunction(ctx)
            ?: throw RuntimeException("Syntax error, jump expression are not allowed to appear at the top level")
        val id = parentFunc.id()
        if (ctx.BREAK() != null ||
            ctx.BREAK_AT() != null ||
            ctx.CONTINUE() != null ||
            ctx.CONTINUE_AT() != null ||
            ctx.RETURN_AT() != null
        ) {
            addArc(id)
        } else {
            addTerminatedNode(id)
        }
    }

    private fun processTryExpression(ctx: TryExpressionContext) {
        if (ctx.children.size == 1) return
        val parentFunc = getParentFunction(ctx)
            ?: throw RuntimeException("Syntax error, try expression are not allowed to appear at the top level")
        val id = parentFunc.id()
        addNode(id, 2)
        addArc(id, 3)
        if (ctx.finallyBlock() != null) {
            addNode(id)
            addArc(id)
        }
    }
}