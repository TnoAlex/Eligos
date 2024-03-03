package com.github.tnoalex.utils.logger

import ch.qos.logback.classic.pattern.*
import ch.qos.logback.classic.pattern.color.HighlightingCompositeConverter
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.CoreConstants
import ch.qos.logback.core.pattern.PatternLayoutBase
import ch.qos.logback.core.pattern.color.*
import ch.qos.logback.core.pattern.parser.Parser

class LogPattern : PatternLayoutBase<ILoggingEvent>() {
    private val defaultConverterMap: MutableMap<String, String> = HashMap()

    init {
        defaultConverterMap.putAll(Parser.DEFAULT_COMPOSITE_CONVERTER_MAP)

        defaultConverterMap["d"] = DateConverter::class.java.name
        defaultConverterMap["date"] = DateConverter::class.java.name

        defaultConverterMap["r"] = RelativeTimeConverter::class.java.name
        defaultConverterMap["relative"] = RelativeTimeConverter::class.java.name

        defaultConverterMap["level"] = LevelConverter::class.java.name
        defaultConverterMap["le"] = LevelConverter::class.java.name
        defaultConverterMap["p"] = LevelConverter::class.java.name

        defaultConverterMap["t"] = ThreadConverter::class.java.name
        defaultConverterMap["thread"] = ThreadConverter::class.java.name

        defaultConverterMap["lo"] = LoggerConverter::class.java.name
        defaultConverterMap["logger"] = LoggerConverter::class.java.name
        defaultConverterMap["c"] = LoggerConverter::class.java.name

        defaultConverterMap["m"] = MessageConverter::class.java.name
        defaultConverterMap["msg"] = MessageConverter::class.java.name
        defaultConverterMap["message"] = MessageConverter::class.java.name

        defaultConverterMap["C"] = ClassOfCallerConverter::class.java.name
        defaultConverterMap["class"] = ClassOfCallerConverter::class.java.name

        defaultConverterMap["M"] = MethodOfCallerConverter::class.java.name
        defaultConverterMap["method"] = MethodOfCallerConverter::class.java.name

        defaultConverterMap["L"] = LineOfCallerConverter::class.java.name
        defaultConverterMap["line"] = LineOfCallerConverter::class.java.name

        defaultConverterMap["F"] = FileOfCallerConverter::class.java.name
        defaultConverterMap["file"] = FileOfCallerConverter::class.java.name

        defaultConverterMap["X"] = MDCConverter::class.java.name
        defaultConverterMap["mdc"] = MDCConverter::class.java.name

        defaultConverterMap["ex"] = ThrowableProxyConverter::class.java.name
        defaultConverterMap["exception"] = ThrowableProxyConverter::class.java.name
        defaultConverterMap["rEx"] = RootCauseFirstThrowableProxyConverter::class.java.name
        defaultConverterMap["rootException"] = RootCauseFirstThrowableProxyConverter::class.java.name
        defaultConverterMap["throwable"] = ThrowableProxyConverter::class.java.name

        defaultConverterMap["xEx"] = ExtendedThrowableProxyConverter::class.java.name
        defaultConverterMap["xException"] = ExtendedThrowableProxyConverter::class.java.name
        defaultConverterMap["xThrowable"] = ExtendedThrowableProxyConverter::class.java.name

        defaultConverterMap["nopex"] = NopThrowableInformationConverter::class.java.name
        defaultConverterMap["nopexception"] = NopThrowableInformationConverter::class.java.name

        defaultConverterMap["cn"] = ContextNameConverter::class.java.name
        defaultConverterMap["contextName"] = ContextNameConverter::class.java.name

        defaultConverterMap["caller"] = CallerDataConverter::class.java.name

        defaultConverterMap["marker"] = MarkerConverter::class.java.name

        defaultConverterMap["property"] = PropertyConverter::class.java.name

        defaultConverterMap["n"] = LineSeparatorConverter::class.java.name

        defaultConverterMap["black"] = BlackCompositeConverter::class.java.name
        defaultConverterMap["red"] = RedCompositeConverter::class.java.name
        defaultConverterMap["green"] = GreenCompositeConverter::class.java.name
        defaultConverterMap["yellow"] = YellowCompositeConverter::class.java.name
        defaultConverterMap["blue"] = BlueCompositeConverter::class.java.name
        defaultConverterMap["magenta"] = MagentaCompositeConverter::class.java.name
        defaultConverterMap["cyan"] = CyanCompositeConverter::class.java.name
        defaultConverterMap["white"] = WhiteCompositeConverter::class.java.name
        defaultConverterMap["gray"] = GrayCompositeConverter::class.java.name
        defaultConverterMap["boldRed"] = BoldRedCompositeConverter::class.java.name
        defaultConverterMap["boldGreen"] = BoldGreenCompositeConverter::class.java.name
        defaultConverterMap["boldYellow"] = BoldYellowCompositeConverter::class.java.name
        defaultConverterMap["boldBlue"] = BoldBlueCompositeConverter::class.java.name
        defaultConverterMap["boldMagenta"] = BoldMagentaCompositeConverter::class.java.name
        defaultConverterMap["boldCyan"] = BoldCyanCompositeConverter::class.java.name
        defaultConverterMap["boldWhite"] = BoldWhiteCompositeConverter::class.java.name
        defaultConverterMap["highlight"] = HighlightingCompositeConverter::class.java.name

        defaultConverterMap["lsn"] = LocalSequenceNumberConverter::class.java.name

        defaultConverterMap["color"] = LogbackColorful::class.java.name
        defaultConverterMap["pid"] = ProcessIdClassicConverter::class.java.name

    }


    override fun getDefaultConverterMap(): Map<String, String> {
        return defaultConverterMap
    }

    override fun doLayout(event: ILoggingEvent?): String? {
        return if (!isStarted) {
            CoreConstants.EMPTY_STRING
        } else writeLoopOnConverters(event)
    }

    override fun getPresentationHeaderPrefix(): String {
        return HEADER_PREFIX
    }

    companion object {
        private const val HEADER_PREFIX = "#logback.classic pattern: "
    }
}