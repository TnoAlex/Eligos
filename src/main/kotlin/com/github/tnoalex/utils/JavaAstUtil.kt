package com.github.tnoalex.utils

import depends.extractor.java.JavaParser.*
import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.RuleContext
import java.util.*

fun getParentClassOrBlockDeclaration(ctx: ParserRuleContext): RuleContext? {
    var parent = ctx.parent
    while (parent != null) {
        if (parent is ClassDeclarationContext || parent is BlockContext || parent is ClassCreatorRestContext || parent is InterfaceDeclarationContext)
            return parent
        parent = parent.parent
    }
    return null
}

fun FormalParameterContext.annotationsOfParam(): LinkedList<String> {
    val annotations = LinkedList<String>()
    variableModifier().forEach { m ->
        m.annotation()?.let { a -> annotations.add(a.qualifiedName().text) }
    }
    return annotations
}

fun FormalParameterContext.isFinal(): Boolean {
    variableModifier().forEach { m ->
        if (m.FINAL() != null) return true
    }
    return false
}

fun LastFormalParameterContext.isFinal(): Boolean {
    variableModifier().forEach { m ->
        if (m.FINAL() != null) return true
    }
    return false
}

fun LastFormalParameterContext.annotationsOfLastParam(): LinkedList<String> {
    val annotations = LinkedList<String>()
    variableModifier().forEach { m ->
        m.annotation()?.let { a -> annotations.add(a.qualifiedName().text) }
    }
    return annotations
}

fun LocalTypeDeclarationContext.annotationsOfLocalType(): LinkedList<String> {
    val annotations = LinkedList<String>()
    classOrInterfaceModifier().forEach {
        it.annotation()?.let { a -> annotations.add(a.qualifiedName().text) }
    }
    return annotations
}

fun LocalTypeDeclarationContext.modifiersOfLocalType(): LinkedList<String> {
    val modifiers = LinkedList<String>()
    classOrInterfaceModifier()?.forEach {
        it.modifier()?.let { m -> modifiers.add(m) }
    }
    return modifiers
}

fun TypeDeclarationContext.annotationsOfType(): LinkedList<String> {
    val annotations = LinkedList<String>()
    classOrInterfaceModifier().forEach {
        it.annotation()?.let { a -> annotations.add(a.qualifiedName().text) }
    }
    return annotations
}

fun ClassBodyDeclarationContext.annotationsOfMember(): LinkedList<String> {
    val annotations = LinkedList<String>()
    modifier().forEach {
        it.classOrInterfaceModifier()?.annotation()?.let { a ->
            annotations.add(a.qualifiedName().text)
        }
    }
    return annotations
}

fun ClassBodyDeclarationContext.modifiersOfMember(): LinkedList<String> {
    val modifiers = LinkedList<String>()
    modifier().forEach {
        modifiers.addAll(it.modifiers())
    }
    return modifiers
}

fun InterfaceBodyDeclarationContext.annotationsOfMember(): LinkedList<String> {
    val annotations = LinkedList<String>()
    modifier().forEach {
        it.classOrInterfaceModifier()?.annotation()?.let { a ->
            annotations.add(a.qualifiedName().text)
        }
    }
    return annotations
}

fun TypeDeclarationContext.modifiersOfType(): LinkedList<String> {
    val modifiers = LinkedList<String>()
    classOrInterfaceModifier().forEach {
        it.modifier()?.let { m -> modifiers.add(m) }
    }
    return modifiers
}

fun ClassOrInterfaceModifierContext.modifier(): String? {
    PUBLIC()?.let { return "public" }
    PROTECTED()?.let { return "protected" }
    PRIVATE()?.let { return "private" }
    STATIC()?.let { return "static" }
    ABSTRACT()?.let { return "abstract" }
    FINAL()?.let { return "final" }
    STRICTFP()?.let { return "strictfp" }
    return null
}

fun ModifierContext.modifiers(): LinkedList<String> {
    val modifiers = LinkedList<String>()
    classOrInterfaceModifier().modifier()?.let { modifiers.add(it) }
    NATIVE()?.let { modifiers.add("native") }
    SYNCHRONIZED().let { modifiers.add("synchronized") }
    TRANSIENT().let { modifiers.add("transient") }
    VOLATILE().let { modifiers.add("volatile") }
    return modifiers
}