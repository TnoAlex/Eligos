package com.github.tnoalex.processor.kotlin

import com.github.tnoalex.elements.AbstractElement
import com.github.tnoalex.elements.BinaryArithmeticExpressionElement
import com.github.tnoalex.elements.FunctionCallElement
import com.github.tnoalex.elements.jvm.kotlin.DeclarationExpressionElement
import com.github.tnoalex.elements.jvm.kotlin.KotlinFunctionElement
import com.github.tnoalex.foundation.eventbus.EventListener
import com.github.tnoalex.processor.AstProcessorWithContext
import com.github.tnoalex.utils.isFunctionCall
import com.github.tnoalex.utils.signature
import depends.extractor.kotlin.KotlinParser.*
import depends.extractor.kotlin.KotlinParserBaseVisitor
import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.RuleContext
import java.util.*
import kotlin.math.exp

class KotlinCallExpressionProcessor : AstProcessorWithContext() {
    override val order: Int
        get() = Int.MAX_VALUE - 2

    @EventListener(eventPrefix = "enter")
    fun findCallExpressionInFunction(ctx: FunctionDeclarationContext) {
        val functionElement = getFunctionElement(ctx.signature()) ?: throw RuntimeException("Inner Error!")
        val functionCallAssign = LinkedList<BinaryArithmeticExpressionElement>()
        ctx.functionBody()?.run {
            block().statements()?.run {
                statement().filter { it.declaration() != null && it.declaration().propertyDeclaration() != null }
                    .map { it.declaration().propertyDeclaration() }
                    .filter { it.ASSIGNMENT() != null }
                    .forEach {
                        processFunctionCallAssign(it, functionElement)
                    }
            }
        }
    }

    private fun processFunctionCallAssign(context: PropertyDeclarationContext, parent: AbstractElement) {
        val assignElement = BinaryArithmeticExpressionElement(
            context.start.line,
            context.stop.line,
            parent,
            "="
        )

        assignElement.setLeftExpression(
            DeclarationExpressionElement(
                context.start.line,
                context.stop.line,
                assignElement,
                if (context.VAL() == null) "var" else "val",
                if (context.variableDeclaration().type() != null) context.variableDeclaration().type().text else null
            )
        )

        val expressions = LinkedList<FunctionCallElement>()
        context.expression().accept(object : KotlinParserBaseVisitor<Unit>() {
            override fun visitPostfixUnaryExpression(ctx: PostfixUnaryExpressionContext) {
                val parentExpression = getNearestParentExpression(ctx)
                if (parentExpression == null || parentExpression.parent != context) return
                //find call expression
                if (ctx.postfixUnarySuffix() != null && ctx.postfixUnarySuffix().isFunctionCall()) {
                    val functionCallElement = FunctionCallElement(
                        ctx.start.line,
                        assignElement,
                        ctx.postfixUnaryExpression().text,
                        LinkedList(
                            ctx.postfixUnarySuffix().callSuffix().valueArguments().valueArgument().map { it.text })
                    )
                    expressions.add(functionCallElement)
                }
                super.visitPostfixUnaryExpression(ctx)
            }
        })

    }


    private fun getFunctionElement(functionSignature: String): KotlinFunctionElement? {
        var function: KotlinFunctionElement? = null
        context.getLastElement().accept { element ->
            if (element is KotlinFunctionElement && element.functionSignature == functionSignature) {
                function = element
                return@accept
            }
        }
        return function
    }

    private fun getNearestParentExpression(ctx: ParserRuleContext): RuleContext? {
        var parent = ctx.parent
        while (parent != null) {
            if (parent is PropertyDeclarationContext)
                return null
            if (parent is ExpressionContext)
                return parent
            parent = parent.parent
        }
        return null
    }
}