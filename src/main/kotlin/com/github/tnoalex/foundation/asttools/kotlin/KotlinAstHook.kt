package com.github.tnoalex.foundation.asttools.kotlin

import com.github.tnoalex.foundation.asttools.AbstractAstHook
import com.github.tnoalex.foundation.asttools.AstProcessor
import depends.extractor.kotlin.KotlinParser.*
import org.antlr.v4.runtime.ParserRuleContext

object KotlinAstHook : AbstractAstHook() {
    fun hookEnterFunctionDeclaration(hook: (FunctionDeclarationContext) -> Unit, process: AstProcessor) {
        val adaptedCallback: (ParserRuleContext) -> Unit = { parentContext ->
            val functionContext: FunctionDeclarationContext = parentContext as FunctionDeclarationContext
            hook(functionContext)
        }
        addHook(getHookedName(this::hookEnterFunctionDeclaration.name), adaptedCallback, process)
    }

    fun hookExitFunctionDeclaration(hook: (FunctionDeclarationContext) -> Unit, process: AstProcessor) {
        val adaptedCallback: (ParserRuleContext) -> Unit = { parentContext ->
            val functionContext: FunctionDeclarationContext = parentContext as FunctionDeclarationContext
            hook(functionContext)
        }
        addHook(getHookedName(this::hookExitFunctionDeclaration.name), adaptedCallback, process)
    }

    fun hookEnterFunctionBody(hook: (FunctionBodyContext) -> Unit, process: AstProcessor) {
        val adaptedCallback: (ParserRuleContext) -> Unit = { parentContext ->
            val functionContext: FunctionBodyContext = parentContext as FunctionBodyContext
            hook(functionContext)
        }
        addHook(getHookedName(this::hookEnterFunctionBody.name), adaptedCallback, process)
    }

    fun hookExitFunctionBody(hook: (FunctionBodyContext) -> Unit, process: AstProcessor) {
        val adaptedCallback: (ParserRuleContext) -> Unit = { parentContext ->
            val functionContext: FunctionBodyContext = parentContext as FunctionBodyContext
            hook(functionContext)
        }
        addHook(getHookedName(this::hookExitFunctionBody.name), adaptedCallback, process)
    }

    fun hookEnterAssignment(hook: (AssignmentContext) -> Unit, process: AstProcessor) {
        val adaptedCallback: (ParserRuleContext) -> Unit = { parentContext ->
            val functionContext: AssignmentContext = parentContext as AssignmentContext
            hook(functionContext)
        }
        addHook(getHookedName(this::hookEnterAssignment.name), adaptedCallback, process)
    }

    fun hookEnterPropertyDeclaration(hook: (PropertyDeclarationContext) -> Unit, process: AstProcessor) {
        val adaptedCallback: (ParserRuleContext) -> Unit = { parentContext ->
            val functionContext: PropertyDeclarationContext = parentContext as PropertyDeclarationContext
            hook(functionContext)
        }
        addHook(getHookedName(this::hookEnterPropertyDeclaration.name), adaptedCallback, process)
    }

    fun hookEnterExpression(hook: (ExpressionContext) -> Unit, process: AstProcessor) {
        val adaptedCallback: (ParserRuleContext) -> Unit = { parentContext ->
            val functionContext: ExpressionContext = parentContext as ExpressionContext
            hook(functionContext)
        }
        addHook(getHookedName(this::hookEnterExpression.name), adaptedCallback, process)
    }

    fun hookEnterElvisExpression(hook: (ElvisExpressionContext) -> Unit, process: AstProcessor) {
        val adaptedCallback: (ParserRuleContext) -> Unit = { parentContext ->
            val functionContext: ElvisExpressionContext = parentContext as ElvisExpressionContext
            hook(functionContext)
        }
        addHook(getHookedName(this::hookEnterElvisExpression.name), adaptedCallback, process)
    }

    fun hookEnterWhenExpression(hook: (WhenExpressionContext) -> Unit, process: AstProcessor) {
        val adaptedCallback: (ParserRuleContext) -> Unit = { parentContext ->
            val functionContext: WhenExpressionContext = parentContext as WhenExpressionContext
            hook(functionContext)
        }
        addHook(getHookedName(this::hookEnterWhenExpression.name), adaptedCallback, process)
    }

    fun hookEnterIfExpression(hook: (IfExpressionContext) -> Unit, process: AstProcessor) {
        val adaptedCallback: (ParserRuleContext) -> Unit = { parentContext ->
            val functionContext: IfExpressionContext = parentContext as IfExpressionContext
            hook(functionContext)
        }
        addHook(getHookedName(this::hookEnterIfExpression.name), adaptedCallback, process)
    }

    fun hookEnterForStatement(hook: (ForStatementContext) -> Unit, process: AstProcessor) {
        val adaptedCallback: (ParserRuleContext) -> Unit = { parentContext ->
            val functionContext: ForStatementContext = parentContext as ForStatementContext
            hook(functionContext)
        }
        addHook(getHookedName(this::hookEnterForStatement.name), adaptedCallback, process)
    }

    fun hookEnterDoWhileStatement(hook: (DoWhileStatementContext) -> Unit, process: AstProcessor) {
        val adaptedCallback: (ParserRuleContext) -> Unit = { parentContext ->
            val functionContext: DoWhileStatementContext = parentContext as DoWhileStatementContext
            hook(functionContext)
        }
        addHook(getHookedName(this::hookEnterDoWhileStatement.name), adaptedCallback, process)
    }

    fun hookEnterWhileStatement(hook: (WhileStatementContext) -> Unit, process: AstProcessor) {
        val adaptedCallback: (ParserRuleContext) -> Unit = { parentContext ->
            val functionContext: WhileStatementContext = parentContext as WhileStatementContext
            hook(functionContext)
        }
        addHook(getHookedName(this::hookEnterWhileStatement.name), adaptedCallback, process)
    }

    fun hookEnterJumpExpression(hook: (JumpExpressionContext) -> Unit, process: AstProcessor) {
        val adaptedCallback: (ParserRuleContext) -> Unit = { parentContext ->
            val functionContext: JumpExpressionContext = parentContext as JumpExpressionContext
            hook(functionContext)
        }
        addHook(getHookedName(this::hookEnterJumpExpression.name), adaptedCallback, process)
    }

    fun hookEnterTryExpression(hook: (TryExpressionContext) -> Unit, process: AstProcessor) {
        val adaptedCallback: (ParserRuleContext) -> Unit = { parentContext ->
            val functionContext: TryExpressionContext = parentContext as TryExpressionContext
            hook(functionContext)
        }
        addHook(getHookedName(this::hookEnterTryExpression.name), adaptedCallback, process)
    }

    private fun getHookedName(funcName: String): String {
        return funcName.replace("hook", "")
    }
}