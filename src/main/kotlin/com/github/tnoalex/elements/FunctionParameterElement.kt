package com.github.tnoalex.elements

class FunctionParameterElement(
    parameterName: String,
    parameterStartLine: Int,
    parameterStopLine: Int,
    val parameterType: String,
    override var parent: AbstractElement?
) : AbstractElement(parameterName, parameterStartLine, parameterStopLine){
    override fun toString(): String {
        return "$elementName:$parameterType"
    }
}