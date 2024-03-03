package com.github.tnoalex.utils.logger

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.pattern.color.ANSIConstants
import ch.qos.logback.core.pattern.color.ForegroundCompositeConverterBase

class LogbackColorful : ForegroundCompositeConverterBase<ILoggingEvent>() {

    override fun getForegroundColorCode(event: ILoggingEvent?): String {
        val level = event?.level ?: Level.INFO
        when (level.toInt()) {
            //ERROR等级为红色
            Level.ERROR_INT -> return ANSIConstants.RED_FG
            //WARN等级为黄色
            Level.WARN_INT ->
                return ANSIConstants.YELLOW_FG
            //INFO等级为蓝色
            Level.INFO_INT ->
                return ANSIConstants.CYAN_FG
            //DEBUG等级为绿色
            Level.DEBUG_INT ->
                return ANSIConstants.GREEN_FG
            //其他为默认颜色
            else ->
                return ANSIConstants.DEFAULT_FG
        }
    }
}