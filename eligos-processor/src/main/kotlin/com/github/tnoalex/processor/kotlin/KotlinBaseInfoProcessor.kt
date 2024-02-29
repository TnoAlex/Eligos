package com.github.tnoalex.processor.kotlin

import com.github.tnoalex.elements.AbstractElement
import com.github.tnoalex.elements.FileElement
import com.github.tnoalex.elements.jvm.kotlin.KotlinClassElement
import com.github.tnoalex.elements.jvm.kotlin.KotlinFunctionElement
import com.github.tnoalex.elements.jvm.kotlin.KotlinParameterElement
import com.github.tnoalex.events.FileEnterEvent
import com.github.tnoalex.events.FileExitEvent
import com.github.tnoalex.foundation.bean.Component
import com.github.tnoalex.foundation.eventbus.EventBus
import com.github.tnoalex.foundation.eventbus.EventListener
import com.github.tnoalex.processor.AbstractBaseInfoProcessor
import com.github.tnoalex.utils.*
import depends.extractor.kotlin.KotlinParser.*
import depends.extractor.kotlin.KotlinParserBaseVisitor
import org.antlr.v4.runtime.ParserRuleContext
import java.util.*

@Component
class KotlinBaseInfoProcessor : AbstractBaseInfoProcessor() {
    override val supportLanguage: List<String>
        get() = listOf("kotlin")

    private lateinit var currentFileContext: KotlinFileContext

    @EventListener("\${currentFileName}.endsWith(\".kt\")", "enter")
    private fun enterKotlinFile(ctx: KotlinFileContext) {
        val fileElement = FileElement(
            currentFileName,
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
        context.addFileElement(fileElement)
        EventBus.post(FileEnterEvent(fileElement))
    }

    private fun createClassElement(ctx: ClassDeclarationContext, parent: AbstractElement) =
        KotlinClassElement(
            ctx.simpleIdentifier().text,
            ctx.start.line,
            ctx.stop.line,
            parent,
            ctx.modifiers()?.annotations() ?: LinkedList(),
            currentFileContext.packageHeader().identifier().text,
            "class",
            ctx.modifiers()?.visibilityModifier(),
            ctx.modifiers()?.classModifier(),
            ctx.modifiers()?.inheritanceModifier(),
            ctx.INTERFACE() != null
        )

    private fun createObjectElement(ctx: ObjectDeclarationContext, parent: AbstractElement) =
        KotlinClassElement(
            ctx.simpleIdentifier().text,
            ctx.start.line,
            ctx.stop.line,
            parent,
            ctx.modifiers()?.annotations() ?: LinkedList(),
            currentFileContext.packageHeader().identifier().text,
            "object",
            ctx.modifiers()?.visibilityModifier(),
            ctx.modifiers()?.classModifier(),
            ctx.modifiers()?.inheritanceModifier(),
            false
        )

    private fun createFunctionParameters(ctx: FunctionValueParametersContext): LinkedList<KotlinParameterElement> {
        val params = LinkedList<KotlinParameterElement>()
        ctx.functionValueParameter()?.forEach {
            params.add(
                KotlinParameterElement(
                    it.parameter().simpleIdentifier().text,
                    it.start.line,
                    it.stop.line,
                    it.parameter().type().text,
                    null,
                    it.parameterModifiers()?.parameterAnnotations() ?: LinkedList(),
                    it.parameterModifiers()?.modifiers()
                )
            )
        }
        return params
    }


    private fun createFunctionElement(ctx: FunctionDeclarationContext, parent: AbstractElement) =
        KotlinFunctionElement(
            ctx.simpleIdentifier().text,
            ctx.start.line,
            ctx.stop.line,
            parent,
            createFunctionParameters(ctx.functionValueParameters()),
            ctx.modifiers()?.annotations() ?: LinkedList(),
            ctx.modifiers()?.visibilityModifier(),
            ctx.modifiers()?.functionModifier(),
            ctx.modifiers()?.inheritanceModifier()
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

    @EventListener("#{fileName}.startsWith(\"exit\") && #{fileName}.endsWith(\".kt\")")
    private fun exitKotlinFile(fileName: String) {
        EventBus.post(FileExitEvent(context.getLastElement()))
    }

}