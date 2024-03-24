package com.github.tnoalex.foundation.bundle.util

import java.text.*
import java.util.*
import kotlin.math.abs


object OrdinalFormat {
    fun apply(format: MessageFormat) {
        val formats = format.formats
        var ordinal: NumberFormat? = null
        for (i in formats.indices) {
            val element = formats[i]
            if ((element is DecimalFormat) && "ordinal" == element.positivePrefix) {
                if (ordinal == null) ordinal = getOrdinalFormat(format.locale)
                format.setFormat(i, ordinal)
            }
        }
    }

    private fun getOrdinalFormat(locale: Locale?): NumberFormat {
        if (locale != null) {
            val language = locale.language
            if ("en" == language ||
                language != null && language.isEmpty()) {
                return EnglishOrdinalFormat()
            }
        }

        return DecimalFormat()
    }

    fun formatEnglish(num: Long): String {
        var mod = (abs(num.toDouble()) % 100).toLong()
        if (mod < 11 || mod > 13) {
            mod = mod % 10
            if (mod == 1L) return num.toString() + "st"
            if (mod == 2L) return num.toString() + "nd"
            if (mod == 3L) return num.toString() + "rd"
        }
        return num.toString() + "th"
    }

    private class EnglishOrdinalFormat : NumberFormat() {
        override fun format(number: Long, toAppendTo: StringBuffer, pos: FieldPosition): StringBuffer {
            return MessageFormat("{0}").format(arrayOf<Any>(formatEnglish(number)), toAppendTo, pos)
        }

        override fun format(number: Double, toAppendTo: StringBuffer, pos: FieldPosition): StringBuffer {
            throw IllegalArgumentException("Cannot format non-integer number")
        }

        override fun parse(source: String, parsePosition: ParsePosition): Number {
            throw UnsupportedOperationException()
        }
    }
}
