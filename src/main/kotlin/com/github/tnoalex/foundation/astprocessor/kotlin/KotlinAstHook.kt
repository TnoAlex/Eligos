package com.github.tnoalex.foundation.astprocessor.kotlin

import com.github.tnoalex.foundation.astprocessor.AbstractAstHook
import depends.extractor.kotlin.KotlinParser.*
import org.antlr.v4.runtime.ParserRuleContext

object KotlinAstHook : AbstractAstHook() {
    fun hookEnterFunctionDeclaration(hook: (FunctionDeclarationContext) -> Unit) {
        val adaptedCallback: (ParserRuleContext) -> Unit = { parentContext ->
            val functionContext: FunctionDeclarationContext = parentContext as FunctionDeclarationContext
            hook(functionContext)
        }
        addHook(getHookedName(this::hookEnterFunctionDeclaration.name), adaptedCallback)
    }

    fun hookExitFunctionDeclaration(hook: (FunctionDeclarationContext) -> Unit) {
        val adaptedCallback: (ParserRuleContext) -> Unit = { parentContext ->
            val functionContext: FunctionDeclarationContext = parentContext as FunctionDeclarationContext
            hook(functionContext)
        }
        addHook(getHookedName(this::hookExitFunctionDeclaration.name), adaptedCallback)
    }

    fun hookEnterFunctionBody(hook: (FunctionBodyContext) -> Unit) {
        val adaptedCallback: (ParserRuleContext) -> Unit = { parentContext ->
            val functionContext: FunctionBodyContext = parentContext as FunctionBodyContext
            hook(functionContext)
        }
        addHook(getHookedName(this::hookEnterFunctionBody.name), adaptedCallback)
    }

    fun hookExitFunctionBody(hook: (FunctionBodyContext) -> Unit) {
        val adaptedCallback: (ParserRuleContext) -> Unit = { parentContext ->
            val functionContext: FunctionBodyContext = parentContext as FunctionBodyContext
            hook(functionContext)
        }
        addHook(getHookedName(this::hookExitFunctionBody.name), adaptedCallback)
    }

    fun hookEnterAssignment(hook: (AssignmentContext) -> Unit) {
        val adaptedCallback: (ParserRuleContext) -> Unit = { parentContext ->
            val functionContext: AssignmentContext = parentContext as AssignmentContext
            hook(functionContext)
        }
        addHook(getHookedName(this::hookEnterAssignment.name), adaptedCallback)
    }

    fun hookEnterPropertyDeclaration(hook: (PropertyDeclarationContext) -> Unit) {
        val adaptedCallback: (ParserRuleContext) -> Unit = { parentContext ->
            val functionContext: PropertyDeclarationContext = parentContext as PropertyDeclarationContext
            hook(functionContext)
        }
        addHook(getHookedName(this::hookEnterPropertyDeclaration.name), adaptedCallback)
    }

    fun hookEnterElvisExpression(hook: (ElvisExpressionContext) -> Unit) {
        val adaptedCallback: (ParserRuleContext) -> Unit = { parentContext ->
            val functionContext: ElvisExpressionContext = parentContext as ElvisExpressionContext
            hook(functionContext)
        }
        addHook(getHookedName(this::hookEnterElvisExpression.name), adaptedCallback)
    }

    fun hookEnterWhenExpression(hook: (WhenExpressionContext) -> Unit) {
        val adaptedCallback: (ParserRuleContext) -> Unit = { parentContext ->
            val functionContext: WhenExpressionContext = parentContext as WhenExpressionContext
            hook(functionContext)
        }
        addHook(getHookedName(this::hookEnterWhenExpression.name), adaptedCallback)
    }

    fun hookEnterIfExpression(hook: (IfExpressionContext) -> Unit) {
        val adaptedCallback: (ParserRuleContext) -> Unit = { parentContext ->
            val functionContext: IfExpressionContext = parentContext as IfExpressionContext
            hook(functionContext)
        }
        addHook(getHookedName(this::hookEnterIfExpression.name), adaptedCallback)
    }

    fun hookEnterForStatement(hook: (ForStatementContext) -> Unit) {
        val adaptedCallback: (ParserRuleContext) -> Unit = { parentContext ->
            val functionContext: ForStatementContext = parentContext as ForStatementContext
            hook(functionContext)
        }
        addHook(getHookedName(this::hookEnterForStatement.name), adaptedCallback)
    }

    fun hookEnterDoWhileStatement(hook: (DoWhileStatementContext) -> Unit) {
        val adaptedCallback: (ParserRuleContext) -> Unit = { parentContext ->
            val functionContext: DoWhileStatementContext = parentContext as DoWhileStatementContext
            hook(functionContext)
        }
        addHook(getHookedName(this::hookEnterDoWhileStatement.name), adaptedCallback)
    }

    fun hookEnterWhileStatement(hook: (WhileStatementContext) -> Unit) {
        val adaptedCallback: (ParserRuleContext) -> Unit = { parentContext ->
            val functionContext: WhileStatementContext = parentContext as WhileStatementContext
            hook(functionContext)
        }
        addHook(getHookedName(this::hookEnterWhileStatement.name), adaptedCallback)
    }

    fun hookEnterJumpExpression(hook: (JumpExpressionContext) -> Unit) {
        val adaptedCallback: (ParserRuleContext) -> Unit = { parentContext ->
            val functionContext: JumpExpressionContext = parentContext as JumpExpressionContext
            hook(functionContext)
        }
        addHook(getHookedName(this::hookEnterJumpExpression.name), adaptedCallback)
    }

    fun hookEnterTryExpression(hook: (TryExpressionContext) -> Unit) {
        val adaptedCallback: (ParserRuleContext) -> Unit = { parentContext ->
            val functionContext: TryExpressionContext = parentContext as TryExpressionContext
            hook(functionContext)
        }
        addHook(getHookedName(this::hookEnterTryExpression.name), adaptedCallback)
    }

    private fun getHookedName(funcName: String): String {
        return funcName.replace("hook", "")
    }
}