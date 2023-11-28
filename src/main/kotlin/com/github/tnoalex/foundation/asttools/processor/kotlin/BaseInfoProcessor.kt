package com.github.tnoalex.foundation.asttools.processor.kotlin

import com.github.tnoalex.elements.AbstractElement
import com.github.tnoalex.elements.ElementContainer
import com.github.tnoalex.elements.FileElement
import com.github.tnoalex.elements.kotlin.KotlinClassElement
import com.github.tnoalex.elements.kotlin.KotlinFunctionElement
import com.github.tnoalex.foundation.asttools.processor.AstProcessor
import com.github.tnoalex.foundation.eventbus.EventBus
import com.github.tnoalex.foundation.eventbus.EventListener
import com.github.tnoalex.utils.*
import depends.extractor.kotlin.KotlinParser.*
import depends.extractor.kotlin.KotlinParserBaseVisitor
import org.antlr.v4.runtime.ParserRuleContext
import java.util.*

class BaseInfoProcessor : AstProcessor {
    override val order: Int
        get() = Int.MAX_VALUE

    override val supportLanguage: List<String>
        get() = listOf("kotlin")

    @EventListener
    private var currentFileName: String = ""

    private lateinit var currentFileContext: KotlinFileContext

    override fun registerListener() {
        EventBus.register(this)
    }

    override fun unregisterListener() {
        EventBus.unregister(this)
    }

    @EventListener
    private fun enterFile(ctx: KotlinFileContext) {
        val fileElement = FileElement(
            currentFileName.split("@")[1],
            ctx.start.line,
            ctx.stop.line
        )
        currentFileContext = ctx
        val innerElement = LinkedList<AbstractElement>()
        ctx.topLevelObject().forEach {
            val declaration = it.declaration() ?: return@forEach
            if (declaration.classDeclaration() != null) {
                val clazzCtx = declaration.classDeclaration()
                val classElement = createClassElement(clazzCtx, fileElement)
                classElement.innerElement.addAll(getInnerElement(clazzCtx, classElement))
                innerElement.add(classElement)
            } else if (declaration.objectDeclaration() != null) {
                val objectCtx = declaration.objectDeclaration()
                val objectElement = createObjectElement(objectCtx, fileElement)
                objectElement.innerElement.addAll(getInnerElement(objectCtx, objectElement))
                innerElement.add(objectElement)
            } else if (declaration.functionDeclaration() != null) {
                val functionCtx = declaration.functionDeclaration()
                val functionElement = createFunctionElement(functionCtx, fileElement)
                functionElement.innerElement.addAll(getInnerElement(functionCtx, functionElement))
                innerElement.add(functionElement)
            } else return@forEach
        }
        fileElement.innerElement.addAll(innerElement)
        ElementContainer.addFileElement(fileElement)
    }

    private fun createClassElement(ctx: ClassDeclarationContext, parent: AbstractElement) =
        KotlinClassElement(
            ctx.simpleIdentifier().text,
            ctx.start.line,
            ctx.stop.line,
            parent,
            currentFileContext.packageHeader().identifier().text,
            "class",
            ctx.modifiers()?.text
        )

    private fun createObjectElement(ctx: ObjectDeclarationContext, parent: AbstractElement) =
        KotlinClassElement(
            ctx.simpleIdentifier().text,
            ctx.start.line,
            ctx.stop.line,
            parent,
            currentFileContext.packageHeader().identifier().text,
            "object",
            ctx.modifiers()?.text
        )

    private fun createFunctionElement(ctx: FunctionDeclarationContext, parent: AbstractElement) =
        KotlinFunctionElement(
            ctx.simpleIdentifier().text,
            ctx.start.line,
            ctx.stop.line,
            parent,
            ctx.paramsNum(),
            ctx.visibilityModifier(),
            ctx.functionModifier(),
            ctx.inheritanceModifier()
        )

    private fun getInnerElement(context: ParserRuleContext, element: AbstractElement): List<AbstractElement> {
        val innerElements = LinkedList<AbstractElement>()
        val innerElementsMap = HashMap<AbstractElement, ParserRuleContext>()

        val visitor = object : KotlinParserBaseVisitor<Unit>() {
            override fun visitFunctionDeclaration(ctx: FunctionDeclarationContext) {
                if (getDirectParentContainer(ctx) == context) {
                    innerElements.add(createFunctionElement(ctx, element))
                    innerElementsMap[innerElements.last] = ctx
                }
                super.visitFunctionDeclaration(ctx)
            }

            override fun visitClassDeclaration(ctx: ClassDeclarationContext) {
                if (getDirectParentContainer(ctx) == context) {
                    innerElements.add(createClassElement(ctx, element))
                    innerElementsMap[innerElements.last] = ctx
                }
                super.visitClassDeclaration(ctx)
            }

            override fun visitObjectDeclaration(ctx: ObjectDeclarationContext) {
                if (getDirectParentContainer(ctx) == context) {
                    innerElements.add(createObjectElement(ctx, element))
                    innerElementsMap[innerElements.last] = ctx
                }
                super.visitObjectDeclaration(ctx)
            }
        }

        when (context) {
            is FunctionDeclarationContext -> {
                context.functionBody()?.accept(visitor)
            }

            is ObjectDeclarationContext -> {
                context.classBody()?.accept(visitor)
            }

            is ClassDeclarationContext -> {
                context.classBody()?.accept(visitor)
            }
        }
        innerElementsMap.forEach { (k, v) ->
            k.innerElement.addAll(getInnerElement(v, k))
        }
        return innerElements
    }

    private fun exitFile(ctx: KotlinFileContext) {

    }
}