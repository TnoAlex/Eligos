package com.github.tnoalex.utils

import org.mvel2.MVEL
import org.mvel2.integration.impl.MapVariableResolverFactory
import java.util.regex.Pattern

fun evaluateBooleanElExpression(rawExpression: String, params: List<Any?>): Boolean {
    val points = extractParams(rawExpression)
    if (points.size != params.size)
        throw RuntimeException("The parameters of the EL expression do not match the given number of parameters")
    val expression = rawExpression.replace("\${", "").replace("}", "")

    val context = points.zip(params).toMap()
    val factory = MapVariableResolverFactory(context)
    val res = MVEL.eval(expression, factory)
    if (res !is Boolean)
        throw RuntimeException("Incorrect El expression parses the value type,expect: Boolean")
    return res
}

private fun extractParams(rawExpression: String): List<String> {
    val regex = "\\$\\{([^}]+)\\}"
    val pattern = Pattern.compile(regex)
    val matcher = pattern.matcher(rawExpression)

    val res = ArrayList<String>()
    while (matcher.find()) {
        val match = matcher.group(1)
        res.add(match)
    }
    return res.distinct()
}
