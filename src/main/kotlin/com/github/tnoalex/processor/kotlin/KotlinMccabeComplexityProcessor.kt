package com.github.tnoalex.processor.kotlin

import com.github.tnoalex.foundation.bean.Component
import com.github.tnoalex.foundation.eventbus.EventListener
import com.github.tnoalex.processor.metrics.MccabeComplexityProcessor
import com.github.tnoalex.utils.getParentFunction
import com.github.tnoalex.utils.id
import com.github.tnoalex.utils.signature
import depends.extractor.kotlin.KotlinParser.*
import org.antlr.v4.runtime.ParserRuleContext

@Component
class KotlinMccabeComplexityProcessor : MccabeComplexityProcessor() {
    override val supportLanguage: List<String>
        get() = listOf("kotlin")

    @EventListener(eventPrefix = "enter")
    private fun processFunctionDeclaration(ctx: FunctionDeclarationContext) {
        val parentFunc = getParentFunction(ctx) // determine whether it is a closure function
        val id = ctx.id()
        addFunction(id, ctx.signature())
        if (parentFunc != null) {
            recordClosurePair(parentFunc.id(), id)
        }
    }

    @EventListener(eventPrefix = "exit")
    private fun processExitFunction(ctx: FunctionDeclarationContext) {
        val id = ctx.id()
        addTerminatedNode(id)
    }

    @EventListener("#{ctx}.children.size > 1")
    private fun processElvisExpression(ctx: ElvisExpressionContext) {
        val parentFunc = getParentFunction(ctx) ?: return // top level property definition or assignment
        val id = parentFunc.id()
        addNode(id)
        addArc(id, 2)
    }

    @EventListener
    private fun processAssignment(ctx: AssignmentContext) {
        processStatement(ctx)
    }

    @EventListener
    private fun processPropertyDeclaration(ctx: PropertyDeclarationContext) {
        processStatement(ctx)
    }

    @EventListener
    private fun processTypeAlias(ctx: TypeAliasContext) {
        processStatement(ctx)
    }

    private fun processStatement(ctx: ParserRuleContext) {
        val parentFunc = getParentFunction(ctx) ?: return
        val id = parentFunc.id()
        addArc(id) //Except for the above expressions, the rest of the expressions are considered linear
        addNode(id)
    }

    @EventListener("#{ctx}.children.size > 1")
    private fun processWhenExpression(ctx: WhenExpressionContext) {
        val parentFunc = getParentFunction(ctx)
            ?: throw RuntimeException("Syntax error, when expression are not allowed to appear at the top level")
        val id = parentFunc.id()
        addNode(id)
        val entrySize = ctx.whenEntry().size
        addArc(id, entrySize * 2)
        addNode(id, entrySize)
    }

    @EventListener("#{ctx}.children.size > 1")
    private fun processIfExpression(ctx: IfExpressionContext) {
        val parentFunc = getParentFunction(ctx) ?: return
        val id = parentFunc.id()
        addNode(id)
        addArc(id, 2)
    }

    @EventListener
    private fun processForStatement(ctx: ForStatementContext) {
        val parentFunc = getParentFunction(ctx)
            ?: throw RuntimeException("Syntax error, for statements are not allowed to appear at the top level")
        val id = parentFunc.id()
        addNode(id)
        addArc(id, 2)
    }

    @EventListener
    private fun processDoWhileStatement(ctx: DoWhileStatementContext) {
        val parentFunc = getParentFunction(ctx)
            ?: throw RuntimeException("Syntax error, do-while statements are not allowed to appear at the top level")
        val id = parentFunc.id()
        addNode(id)
        addArc(id, 2)
    }

    @EventListener
    private fun processWhileStatement(ctx: WhileStatementContext) {
        val parentFunc = getParentFunction(ctx)
            ?: throw RuntimeException("Syntax error, while statements are not allowed to appear at the top level")
        val id = parentFunc.id()
        addNode(id)
        addArc(id, 2)
    }

    @EventListener("#{ctx}.children.size > 1")
    private fun processJumpExpression(ctx: JumpExpressionContext) {
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

    @EventListener("#{ctx}.children.size > 1")
    private fun processTryExpression(ctx: TryExpressionContext) {
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