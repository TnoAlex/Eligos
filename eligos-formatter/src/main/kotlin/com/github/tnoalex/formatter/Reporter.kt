package com.github.tnoalex.formatter

import com.github.tnoalex.specs.FormatterSpec

class Reporter(private val formatterSpec: FormatterSpec) {

    private val currentFormatter = getFormatter(formatterSpec.resultFormat)
    fun report() {
        format()
        write()
    }

    private fun format() {

    }

    private fun write() {

    }

    companion object {
        fun getFormatter(type: FormatterTypeEnum): IFormatter {
            if (type == FormatterTypeEnum.JSON) {
                return JsonFormatter()
            }
            throw RuntimeException("Can not found formatter with type $type")
        }
    }
}