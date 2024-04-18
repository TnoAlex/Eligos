package com.github.tnoalex

import com.github.tnoalex.foundation.EligosBeforeAllTestExtension
import com.github.tnoalex.foundation.RequireTestProcessor
import com.github.tnoalex.processor.common.CircularReferencesProcessor
import com.github.tnoalex.processor.common.TooManyParametersProcessor
import com.github.tnoalex.processor.common.UnUsedImportProcessor
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode

@ExtendWith(EligosBeforeAllTestExtension::class)
@Execution(ExecutionMode.SAME_THREAD)
class CommonProcessorTest {

    @RequireTestProcessor("resources@complexMethods")
    fun testTooManyParameters(processor: TooManyParametersProcessor) {

    }

    @RequireTestProcessor("resources@circularRceferences")
    fun testCircularReferences(processor: CircularReferencesProcessor) {

    }

    @RequireTestProcessor("resources@unusedImport")
    fun testUnUsedImport(processor: UnUsedImportProcessor) {

    }
}