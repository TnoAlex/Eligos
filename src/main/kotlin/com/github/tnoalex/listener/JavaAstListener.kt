package com.github.tnoalex.listener

import com.github.tnoalex.foundation.eventbus.EventBus
import depends.extractor.java.JavaParser
import depends.extractor.java.JavaParserBaseListener


class JavaAstListener : AstListener, JavaParserBaseListener() {
    override val supportLanguage: List<String>
        get() = listOf("java")

    override fun enterCompilationUnit(ctx: JavaParser.CompilationUnitContext) {
        EventBus.post(ctx, prefix = "enter")
        super.enterCompilationUnit(ctx)
    }

    override fun exitCompilationUnit(ctx: JavaParser.CompilationUnitContext) {
        EventBus.post(ctx, prefix = "exit")
        super.exitCompilationUnit(ctx)
    }

    override fun enterClassDeclaration(ctx: JavaParser.ClassDeclarationContext) {
        EventBus.post(ctx)
        super.enterClassDeclaration(ctx)
    }

    override fun enterMemberDeclaration(ctx: JavaParser.MemberDeclarationContext) {
        EventBus.post(ctx)
        super.enterMemberDeclaration(ctx)
    }

    override fun enterStatement(ctx: JavaParser.StatementContext) {
        EventBus.post(ctx)
        super.enterStatement(ctx)
    }

    override fun enterVariableDeclarator(ctx: JavaParser.VariableDeclaratorContext) {
        EventBus.post(ctx)
        super.enterVariableDeclarator(ctx)
    }
}