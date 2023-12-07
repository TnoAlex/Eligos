package com.github.tnoalex.elements.jvm.kotlin

import com.github.tnoalex.elements.AbstractElement
import java.util.*

class KotlinFunctionElement(
    functionName: String,
    functionStartLine: Int,
    functionStopLine: Int,
    parent: AbstractElement,
    val parameters: LinkedList<KotlinParameterElement>,
    annotations: LinkedList<String>,
    visibilityModifier: String?,
    functionModifier: String?,
    inheritanceModifier: String?
) : KotlinElement(
    functionName,
    functionStartLine,
    functionStopLine,
    parent,
    annotations,
    visibilityModifier,
    functionModifier?.let{ listOf(functionModifier) },
    inheritanceModifier
) {
    init {
        parameters.forEach {
            it.parent = this
        }
    }

    val functionSignature: String
        get() {
            val sb = StringBuilder("$elementName(")
            parameters.forEach {
                sb.append(it.toString()).append(",")
            }
            if (parameters.isNotEmpty())
                sb[sb.lastIndex] = ')'
            else
                sb.append(")")
            return sb.toString()
        }

    val isTopLevel: Boolean
        get() = parent is KotlinFunctionElement


    override fun toString(): String {
        return functionSignature
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as KotlinFunctionElement

        return parameters == other.parameters
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + parameters.hashCode()
        return result
    }
}