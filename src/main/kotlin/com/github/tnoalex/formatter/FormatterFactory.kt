package com.github.tnoalex.formatter

import com.github.tnoalex.entity.enums.FormatterTypeEnum
import com.github.tnoalex.formatter.json.JsonFormatter

object FormatterFactory {
    fun getFormatter(type: FormatterTypeEnum): IFormatter? {
        if (type == FormatterTypeEnum.JSON) {
            return JsonFormatter()
        }
        return null
    }
}