package com.github.tnoalex.processor

import com.github.tnoalex.Analyzer
import com.github.tnoalex.config.ConfigParser
import com.github.tnoalex.formatter.json.JsonFormatter
import com.github.tnoalex.foundation.ApplicationContext
import com.github.tnoalex.foundation.filetools.FileContainer
import com.github.tnoalex.utils.StdOutErrWrapper
import depends.LangRegister
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.io.File

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class JavaProcessorTest {
    private var analyzer = Analyzer(JsonFormatter(), listOf("kotlin","java"))

    init {
        StdOutErrWrapper.init()
        LangRegister.register()

        FileContainer.initFileContainer(
            File("E:\\code\\depends-smell\\src\\test\\resources\\java-code-samples"),
            File("./"), "out"
        )
        analyzer.analyze()
    }

    @Test
    fun testBaseInfo(){
        val file = analyzer.getContext()
            .getFileElement("E:\\code\\depends-smell\\src\\test\\resources\\java-code-samples\\javabaseinfos\\JavaBaseInfoSample0.java")
        println()
    }
}