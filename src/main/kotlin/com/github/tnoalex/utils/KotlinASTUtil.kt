package com.github.tnoalex.utils

import depends.extractor.kotlin.KotlinParser.*
import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.RuleContext

fun getCurrentFunction(ctx: RuleContext): FunctionDeclarationContext? {
    var parent = ctx.parent
    while (parent != null) {
        if (parent is FunctionDeclarationContext) {
            return parent
        }
        parent = parent.parent
    }
    return null
}


fun getCurrentClass(ctx: ParserRuleContext): ClassDeclarationContext? {
    var parent = ctx.parent
    while (parent != null) {
        if (parent is ClassDeclarationContext) {
            return parent
        }
        parent = parent.parent
    }
    return null
}

fun getCurrentPackageName(ctx: ParserRuleContext): String? {
    var parent = ctx.parent
    while (parent != null) {
        if (parent is KotlinFileContext) {
            return parent.packageHeader().identifier().text
        }
        parent = parent.parent
    }
    return null
}

fun FunctionDeclarationContext.visibilityModifier(): String? {
    return modifiers()?.modifier()?.firstOrNull { it.visibilityModifier() != null }?.text
}

fun FunctionDeclarationContext.functionModifier(): String? {
    return modifiers()?.modifier()?.firstOrNull { it.functionModifier() != null }?.text
}

fun FunctionDeclarationContext.inheritanceModifier(): String? {
    return modifiers()?.modifier()?.firstOrNull { it.inheritanceModifier() != null }?.text
}

fun FunctionDeclarationContext.paramsNum(): Int {
    return functionValueParameters().functionValueParameter().size
}

fun FunctionDeclarationContext.id(): String {
    return simpleIdentifier().text + paramsNum()
}

fun ExpressionContext.ifExpression(): IfExpressionContext? {
    children.forEach {
        if (it is IfExpressionContext) {
            return it
        }
    }
    return null
}

fun ExpressionContext.whenExpression(): WhenExpressionContext? {
    children.forEach {
        if (it is WhenExpressionContext)
            return it
    }
    return null
}

fun ExpressionContext.tryExpression(): TryExpressionContext? {
    children.forEach {
        if (it is TryExpressionContext)
            return it
    }
    return null
}

fun ExpressionContext.jumpExpression(): JumpExpressionContext? {
    children.forEach {
        if (it is JumpExpressionContext)
            return it
    }
    return null
}

fun ExpressionContext.isPropertyOrFunctionCall() {

}

fun ExpressionContext.isElvisExpression(): ElvisExpressionContext? {
    children.forEach {
        if (it is ElvisExpressionContext)
            return it
    }
    return null
}