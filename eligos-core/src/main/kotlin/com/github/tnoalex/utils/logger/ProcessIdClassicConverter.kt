package com.github.tnoalex.utils.logger

import ch.qos.logback.classic.pattern.ClassicConverter
import ch.qos.logback.classic.spi.ILoggingEvent
import java.lang.management.ManagementFactory

class ProcessIdClassicConverter : ClassicConverter() {
    override fun convert(event: ILoggingEvent?): String {
        val runtime = ManagementFactory.getRuntimeMXBean()
        val name: String = runtime.name
        return name.substring(0, name.indexOf("@"))
    }
}