package com.github.tnoalex.utils

import org.mvel2.MVEL
import org.mvel2.integration.impl.MapVariableResolverFactory
import java.util.regex.Pattern

fun evaluateBooleanElExpression(rawExpression: String, vararg args: Any?): Boolean {
    val res = eval(rawExpression, *args)
    if (res !is Boolean)
        throw RuntimeException("Incorrect El expression parses the value type,expect: Boolean")
    return res
}

private fun eval(rawExpression: String, vararg args: Any?): Any? {
    val context = HashMap<String, Any?>()
    if (args.isNotEmpty()) {
        if (rawExpression.contains("\$")) {
            val params = extractParams(rawExpression, CLASS_SCOOP_REGEX)
            params.forEach {
                val property = getClassPropertyByName(args[0]!!::class, it).filter { p -> p.parameters.size == 1 }
                if (property.isEmpty()) {
                    throw RuntimeException("Can not find a property named $it in ${args[0]!!::class.simpleName}")
                }
                context[it] = invokePropertyGetter(args[0]!!, property[0])
            }
        }
        if (rawExpression.contains("#")) {
            val params = extractParams(rawExpression, PARMA_SCOOP_REGEX)
            if (params.size != args.size - 1) {
                throw RuntimeException("The number of parameters does not matched")
            }
            params.zip(args.slice(1..<args.size)).forEach { (f, s) ->
                context[f] = s
            }
        }
    }
    val expression = rawExpression.replace("#{", "").replace("\${", "").replace("}", "")
    val factory = MapVariableResolverFactory(context)
    return MVEL.eval(expression, factory)
}

private fun extractParams(rawExpression: String, regex: String): List<String> {
    val pattern = Pattern.compile(regex)
    val matcher = pattern.matcher(rawExpression)

    val res = ArrayList<String>()
    while (matcher.find()) {
        val match = matcher.group(1)
        res.add(match)
    }
    return res.distinct()
}

private const val CLASS_SCOOP_REGEX = "\\$\\{([^}]+)\\}"
private const val PARMA_SCOOP_REGEX = "#\\{([^}]+)\\}"