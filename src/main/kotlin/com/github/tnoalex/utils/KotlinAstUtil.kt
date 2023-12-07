package com.github.tnoalex.utils

import depends.extractor.kotlin.KotlinParser.*
import depends.extractor.kotlin.KotlinParserBaseVisitor
import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.RuleContext
import java.util.*

fun getParentFunction(ctx: ParserRuleContext): FunctionDeclarationContext? {
    var parent = ctx.parent
    while (parent != null) {
        if (parent is FunctionDeclarationContext) {
            return parent
        }
        parent = parent.parent
    }
    return null
}

fun getDirectParentContainer(ctx: ParserRuleContext): RuleContext? {
    var parent = ctx.parent
    while (parent != null) {
        if (parent is FunctionDeclarationContext ||
            parent is ClassDeclarationContext ||
            parent is ObjectDeclarationContext
        ) {
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

fun FunctionDeclarationContext.paramsNum(): Int {
    return functionValueParameters().functionValueParameter().size
}

fun FunctionDeclarationContext.id(): String {
    return "${simpleIdentifier().text}@${paramsNum()}:${start.line}-${stop.line}"
}

fun FunctionDeclarationContext.signature(): String {
    return "${simpleIdentifier().text}${functionValueParameters().text}"
}

fun FunctionDeclarationContext.isClosure(): Boolean {
    var parent = parent
    while (parent != null) {
        if (parent is FunctionDeclarationContext) {
            return true
        }
        parent = parent.parent
    }
    return false
}


fun ExpressionContext.nearestIfExpression(): IfExpressionContext? {
    var ifExpressionContext: IfExpressionContext? = null
    var flag = false
    accept(object : KotlinParserBaseVisitor<Unit>() {
        override fun visitIfExpression(ctx: IfExpressionContext?) {
            if (flag) return
            ifExpressionContext = ctx
            flag = true
            super.visitIfExpression(ctx)
        }
    })
    return ifExpressionContext
}

fun ExpressionContext.nearestWhenExpression(): WhenExpressionContext? {
    var whenExpressionContext: WhenExpressionContext? = null
    var flag = false
    accept(object : KotlinParserBaseVisitor<Unit>() {
        override fun visitWhenExpression(ctx: WhenExpressionContext?) {
            if (flag) return
            whenExpressionContext = ctx
            flag = true
            super.visitWhenExpression(ctx)
        }
    })
    return whenExpressionContext
}

fun ExpressionContext.nearestTryExpression(): TryExpressionContext? {
    var tryExpressionContext: TryExpressionContext? = null
    var flag = false
    accept(object : KotlinParserBaseVisitor<Unit>() {
        override fun visitTryExpression(ctx: TryExpressionContext?) {
            if (flag) return
            tryExpressionContext = ctx
            flag = true
            super.visitTryExpression(ctx)
        }
    })
    return tryExpressionContext
}

fun ExpressionContext.nearestJumpExpression(): JumpExpressionContext? {
    var jumpExpressionContext: JumpExpressionContext? = null
    var flag = false
    accept(object : KotlinParserBaseVisitor<Unit>() {
        override fun visitJumpExpression(ctx: JumpExpressionContext?) {
            if (flag) return
            jumpExpressionContext = ctx
            flag = true
            super.visitJumpExpression(ctx)
        }
    })
    return jumpExpressionContext
}

fun ExpressionContext.nearestElvisExpression(): ElvisExpressionContext? {
    var elvisExpressionContext: ElvisExpressionContext? = null
    var flag = false
    accept(object : KotlinParserBaseVisitor<Unit>() {
        override fun visitElvisExpression(ctx: ElvisExpressionContext?) {
            if (flag) return
            elvisExpressionContext = ctx
            flag = true
            super.visitElvisExpression(ctx)
        }
    })
    return elvisExpressionContext
}

fun ExpressionContext.nearestCallSuffixExpression(): CallSuffixContext? {
    var callSuffixContext: CallSuffixContext? = null
    var flag = false
    accept(object : KotlinParserBaseVisitor<Unit>() {
        override fun visitCallSuffix(ctx: CallSuffixContext?) {
            if (flag) return
            callSuffixContext = ctx
            flag = true
            super.visitCallSuffix(ctx)
        }
    })
    return callSuffixContext
}

fun PostfixUnaryExpressionContext.independent(): Boolean {
    var parent = parent

    while (parent !is ExpressionContext) {
        when (parent) {
            is PrefixUnaryExpressionContext ->
                if (!parent.unaryPrefix().isNullOrEmpty()) return false

            is AsExpressionContext ->
                if (!parent.asOperator().isNullOrEmpty()) return false

            is MultiplicativeExpressionContext ->
                if (!parent.multiplicativeOperator().isNullOrEmpty()) return false

            is AdditiveExpressionContext ->
                if (!parent.additiveOperator().isNullOrEmpty()) return false

            is RangeExpressionContext ->
                if (!(parent.RANGE().isNullOrEmpty() || parent.RANGE_UNTIL().isNullOrEmpty())) return false

            is InfixFunctionCallContext ->
                if (!parent.simpleIdentifier().isNullOrEmpty()) return false

            is ElvisExpressionContext ->
                if (!parent.elvis().isNullOrEmpty()) return false

            is InfixOperationContext ->
                if (!parent.inOperator().isNullOrEmpty()) return false

            is GenericCallLikeComparisonContext ->
                if (!parent.callSuffix().isNullOrEmpty()) return false

            is ComparisonContext ->
                if (!parent.comparisonOperator().isNullOrEmpty()) return false

            is EqualityContext ->
                if (!parent.equalityOperator().isNullOrEmpty()) return false

            is ConjunctionContext ->
                if (!parent.CONJ().isNullOrEmpty()) return false

            is DisjunctionContext ->
                if (!parent.DISJ().isNullOrEmpty()) return false
        }
        parent = parent.parent
    }
    return true
}

fun ModifiersContext.annotations(): LinkedList<String> {
    val res = LinkedList<String>()
    annotation().forEach {
        if (it.singleAnnotation() != null) {
            res.add(it.singleAnnotation().text)
        }
        if (it.multiAnnotation() != null) {
            it.multiAnnotation().unescapedAnnotation().forEach { ua ->
                res.add(ua.text)
            }
        }
    }
    return res
}

fun ModifiersContext.visibilityModifier(): String? {
    return modifier().firstOrNull { it.visibilityModifier() != null }?.text
}

fun ModifiersContext.classModifier(): String? {
    return modifier().firstOrNull { it.classModifier() != null }?.text
}

fun ModifiersContext.functionModifier(): String? {
    return modifier().firstOrNull { it.functionModifier() != null }?.text
}
fun ModifiersContext.inheritanceModifier(): String? {
    return modifier().firstOrNull { it.inheritanceModifier() != null }?.text
}

fun ParameterModifiersContext.modifiers(): List<String> {
    return parameterModifier().map { it.text }
}

fun ParameterModifiersContext.parameterAnnotations(): LinkedList<String> {
    val res = LinkedList<String>()
    annotation().forEach {
        if (it.singleAnnotation() != null) {
            res.add(it.singleAnnotation().text)
        }
        if (it.multiAnnotation() != null) {
            it.multiAnnotation().unescapedAnnotation().forEach { ua ->
                res.add(ua.text)
            }
        }
    }
    return res
}