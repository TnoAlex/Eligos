package com.github.tnoalex.processor.java

import com.github.tnoalex.elements.AbstractElement
import com.github.tnoalex.elements.FileElement
import com.github.tnoalex.elements.jvm.java.JavaClassElement
import com.github.tnoalex.elements.jvm.java.JavaElement
import com.github.tnoalex.elements.jvm.java.JavaFunctionElement
import com.github.tnoalex.elements.jvm.java.JavaParameterElement
import com.github.tnoalex.foundation.eventbus.EventListener
import com.github.tnoalex.processor.AbstractBaseInfoProcessor
import com.github.tnoalex.utils.*
import depends.extractor.java.JavaParser.*
import depends.extractor.java.JavaParserBaseVisitor
import org.antlr.v4.runtime.ParserRuleContext
import java.util.*

class JavaBaseInfoProcessor : AbstractBaseInfoProcessor() {
    override val order: Int
        get() = Int.MAX_VALUE
    override val supportLanguage: List<String>
        get() = listOf("java")

    private lateinit var currentFileContext: CompilationUnitContext

    @EventListener("\${currentFileName}.endsWith(\".java\")", "enter")
    private fun enterJavaFile(ctx: CompilationUnitContext) {
        currentFileContext = ctx
        val fileElement = FileElement(
            currentFileName,
            ctx.start.line,
            ctx.stop.line
        )
        val innerElement = LinkedList<JavaElement>()
        ctx.typeDeclaration().forEach {
            // Only class will be parsed yet
            if (it.classDeclaration() != null) {
                val classElement = createClassElement(it, fileElement, false)
                classElement.innerElement.addAll(getClassOrInterFaceInnerElement(it.classDeclaration(), classElement))
                innerElement.add(classElement)
            }
            if (it.interfaceDeclaration() == null) return@forEach
            val interfaceElement = createClassElement(it, fileElement, true)
            interfaceElement.innerElement.addAll(
                getClassOrInterFaceInnerElement(
                    it.interfaceDeclaration(),
                    interfaceElement
                )
            )
            innerElement.add(interfaceElement)
        }
        fileElement.innerElement.addAll(innerElement)
        context.addFileElement(fileElement)

    }

    private fun createClassElement(
        ctx: TypeDeclarationContext,
        parent: AbstractElement,
        isInterface: Boolean
    ): JavaClassElement {
        return JavaClassElement(
            if (!isInterface) ctx.classDeclaration().IDENTIFIER().text else ctx.interfaceDeclaration().IDENTIFIER().text,
            ctx.start.line,
            ctx.stop.line,
            parent,
            ctx.annotationsOfType(),
            ctx.modifiersOfType(),
            currentFileContext.packageDeclaration().qualifiedName().text,
            isInterface
        )
    }

    private fun createClassElement(
        ctx: ParserRuleContext,
        elementParent: AbstractElement
    ): JavaClassElement {
        if (ctx !is ClassDeclarationContext && ctx !is InterfaceDeclarationContext)
            throw RuntimeException("Unsupported parser tree context")
        val annotations: LinkedList<String>
        val modifiers: LinkedList<String>
        when (val parent = ctx.parent) {
            is LocalTypeDeclarationContext -> {
                annotations = parent.annotationsOfLocalType()
                modifiers = parent.modifiersOfLocalType()
            }

            is MemberDeclarationContext -> {
                annotations = (parent.parent as ClassBodyDeclarationContext).annotationsOfMember()
                modifiers = (parent.parent as ClassBodyDeclarationContext).modifiersOfMember()
            }

            is InterfaceMemberDeclarationContext -> {
                annotations = (parent.parent as InterfaceBodyDeclarationContext).annotationsOfMember()
                modifiers = LinkedList()
            }

            else -> throw RuntimeException("Unsupported parser tree context")
        }
        when (ctx) {
            is ClassDeclarationContext -> return JavaClassElement(
                ctx.IDENTIFIER().text,
                ctx.start.line,
                ctx.stop.line,
                elementParent,
                annotations,
                modifiers,
                currentFileContext.packageDeclaration().qualifiedName().text,
                false
            )

            is InterfaceDeclarationContext -> return JavaClassElement(
                ctx.IDENTIFIER().text,
                ctx.start.line,
                ctx.stop.line,
                elementParent,
                annotations,
                modifiers,
                currentFileContext.packageDeclaration().qualifiedName().text,
                true
            )

            else -> throw RuntimeException("Unsupported parser tree context")
        }
    }

    private fun createBlockElement(ctx: BlockContext, parent: JavaElement): JavaElement {
        return JavaElement(
            null,
            ctx.start.line,
            ctx.stop.line,
            parent,
            LinkedList(),
            (ctx.parent as ClassBodyDeclarationContext).STATIC()?.let { LinkedList(listOf("static")) }
        )
    }

    private fun createParameterElements(ctx: FormalParametersContext): LinkedList<JavaParameterElement> {
        val params = LinkedList<JavaParameterElement>()
        ctx.formalParameterList()?.run {
            formalParameter().forEach {
                params.add(
                    JavaParameterElement(
                        it.variableDeclaratorId().IDENTIFIER().text,
                        it.start.line,
                        it.stop.line,
                        it.typeType().text,
                        false,
                        null,
                        it.annotationsOfParam(),
                        if (it.isFinal()) "final" else null
                    )
                )
            }
            lastFormalParameter()?.let {
                params.add(
                    JavaParameterElement(
                        it.variableDeclaratorId().IDENTIFIER().text,
                        it.start.line,
                        it.stop.line,
                        it.typeType().text,
                        true,
                        null,
                        it.annotationsOfLastParam(),
                        if (it.isFinal()) "final" else null
                    )
                )
            }
        }
        return params
    }

    private fun createFunctionElement(ctx: MethodDeclarationContext, parent: JavaElement): JavaFunctionElement {
        val parentCtx = ctx.parent.parent as ClassBodyDeclarationContext
        return JavaFunctionElement(
            ctx.IDENTIFIER().text,
            ctx.start.line,
            ctx.stop.line,
            createParameterElements(ctx.formalParameters()),
            parent,
            parentCtx.annotationsOfMember(),
            parentCtx.modifiersOfMember()
        )
    }

    private fun createAnonymousInnerClass(ctx: CreatorContext, parent: AbstractElement): JavaClassElement {
        return JavaClassElement(
            null,
            ctx.start.line,
            ctx.stop.line,
            parent,
            LinkedList(),
            LinkedList(),
            currentFileContext.packageDeclaration().qualifiedName().text,
            false,
            ctx.createdName().text
        )
    }

    private fun getClassOrInterFaceInnerElement(
        context: ParserRuleContext,
        parent: JavaElement
    ): LinkedList<JavaElement> {
        val innerElements = LinkedList<JavaElement>()
        val innerMap = HashMap<JavaElement, ParserRuleContext>()
        val visitor = object : JavaParserBaseVisitor<Unit>() {
            override fun visitBlock(ctx: BlockContext) {
                if (ctx.parent !is ClassBodyDeclarationContext) return
                if (getParentClassOrBlockDeclaration(ctx) == context) {
                    innerElements.add(createBlockElement(ctx, parent))
                    innerMap[innerElements.last] = ctx
                }
                super.visitBlock(ctx)
            }

            override fun visitClassDeclaration(ctx: ClassDeclarationContext) {
                if (getParentClassOrBlockDeclaration(ctx) == context) {
                    innerElements.add(createClassElement(ctx, parent))
                    innerMap[innerElements.last] = ctx
                }
                super.visitClassDeclaration(ctx)
            }

            override fun visitMethodDeclaration(ctx: MethodDeclarationContext) {
                if (getParentClassOrBlockDeclaration(ctx) == context) {
                    innerElements.add(createFunctionElement(ctx, parent))
                    innerMap[innerElements.last] = ctx
                }
                super.visitMethodDeclaration(ctx)
            }

            override fun visitClassCreatorRest(ctx: ClassCreatorRestContext) {
                if (getParentClassOrBlockDeclaration(ctx) == context) {
                    val creator = ctx.parent as CreatorContext
                    innerElements.add(createAnonymousInnerClass(creator, parent))
                    innerMap[innerElements.last] = ctx
                }
                super.visitClassCreatorRest(ctx)
            }

            override fun visitInterfaceDeclaration(ctx: InterfaceDeclarationContext) {
                if (getParentClassOrBlockDeclaration(ctx) == context) {
                    innerElements.add(createClassElement(ctx, parent))
                    innerMap[innerElements.last] = ctx
                }
                super.visitInterfaceDeclaration(ctx)
            }
        }
        context.accept(visitor)
        innerMap.forEach { (k, v) ->
            k.innerElement.addAll(getClassOrInterFaceInnerElement(v, k))
        }
        return innerElements
    }
}