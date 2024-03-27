package com.github.tnoalex.formatter.utils

import org.apache.commons.text.StringEscapeUtils

fun encodeHtml(obj: Any): Any {
    return encodeObj(obj, StringEscapeUtils::escapeHtml4)
}

fun encodeXml(obj: Any): Any {
    return encodeObj(obj, StringEscapeUtils::escapeXml11)
}

private fun encodeObj(obj: Any, encoder: (String) -> String): Any {
    return when (obj) {
        is Number -> {
            obj
        }

        is String -> {
            encoder(obj)
        }

        is List<*> -> {
            val encodeList = ArrayList<Any>()
            obj.forEach {
                encodeList.add(encodeObj(it!!, encoder))
            }
            encodeList
        }

        is Map<*, *> -> {
            val encodeMap = LinkedHashMap<Any, Any>()
            obj.forEach { (k, v) ->
                encodeMap[encodeObj(k!!, encoder)] = encodeObj(v!!, encoder)
            }
            encodeMap
        }

        else -> {
            obj.toString()
        }
    }
}