package com.github.tnoalex.processor.stats

import com.github.tnoalex.events.AllFileParsedEvent
import com.github.tnoalex.foundation.LaunchEnvironment
import com.github.tnoalex.foundation.bean.Component
import com.github.tnoalex.foundation.bean.Suitable
import com.github.tnoalex.foundation.eventbus.EventListener
import com.github.tnoalex.processor.BaseProcessor
import com.github.tnoalex.processor.utils.lineCount
import com.github.tnoalex.statistics.KotlinStatistics
import org.jetbrains.kotlin.psi.*


@Component(order = -1)
@Suitable(LaunchEnvironment.CLI)
class KotlinStatisticsProcessor : BaseProcessor {
    private var stats = KotlinStatistics()

    @EventListener(order = -1)
    fun process(ktFile: KtFile) {
        stats.fileNumber++
        stats.lineNumber += ktFile.lineCount
        ktFile.accept(ktVisitor)
    }

    @EventListener(order = -1)
    fun onFileFinishEvent(event: AllFileParsedEvent) {
        context.reportStatistics(stats)
        stats = KotlinStatistics()
    }

    private val ktVisitor = object : KtTreeVisitorVoid() {
        override fun visitClass(klass: KtClass) {
            stats.classNumber++
            super.visitClass(klass)
        }

        override fun visitProperty(property: KtProperty) {
            stats.propertyNumber++
            super.visitProperty(property)
        }

        override fun visitNamedFunction(function: KtNamedFunction) {
            stats.functionNumber++
            super.visitNamedFunction(function)
        }
    }
}