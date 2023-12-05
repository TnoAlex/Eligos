package com.github.tnoalex.elements.kotlin

import com.github.tnoalex.elements.AbstractElement
import com.github.tnoalex.elements.FunctionParameterElement
import java.util.*

class KotlinFunctionElement(
    functionName: String,
    functionStartLine: Int,
    functionStopLine: Int,
    override var parent: AbstractElement?,
    val parameters: LinkedList<FunctionParameterElement>,
    private val visibility: String?,
    private val functionModifier: String?,
    private val inheritanceModifier: String?
) : AbstractElement(functionName, functionStartLine, functionStopLine) {
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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as KotlinFunctionElement

        if (parent != other.parent) return false
        if (parameters != other.parameters) return false
        if (visibility != other.visibility) return false
        if (functionModifier != other.functionModifier) return false
        if (inheritanceModifier != other.inheritanceModifier) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + (parent?.hashCode() ?: 0)
        result = 31 * result + parameters.hashCode()
        result = 31 * result + (visibility?.hashCode() ?: 0)
        result = 31 * result + (functionModifier?.hashCode() ?: 0)
        result = 31 * result + (inheritanceModifier?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return functionSignature
    }
}