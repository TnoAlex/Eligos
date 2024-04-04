package com.github.tnoalex.processor.stats

import com.github.tnoalex.events.AllFileParsedEvent
import com.github.tnoalex.foundation.LaunchEnvironment
import com.github.tnoalex.foundation.bean.Component
import com.github.tnoalex.foundation.bean.Suitable
import com.github.tnoalex.foundation.eventbus.EventListener
import com.github.tnoalex.processor.BaseProcessor
import com.github.tnoalex.processor.utils.lineCount
import com.github.tnoalex.statistics.JavaStatistics
import com.intellij.psi.JavaRecursiveElementVisitor
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.PsiMethod

@Component(order = -1)
@Suitable(LaunchEnvironment.CLI)
class JavaStatisticsProcessor : BaseProcessor {
    private var stats = JavaStatistics()

    @EventListener(order = -1)
    fun process(javaFile: PsiJavaFile) {
        stats.fileNumber++
        stats.lineNumber += javaFile.lineCount
        javaFile.accept(javaVisitor)
    }

    @EventListener(order = -1)
    fun onFileFinishEvent(event: AllFileParsedEvent) {
        context.reportStatistics(stats)
        stats = JavaStatistics()
    }

    private val javaVisitor = object : JavaRecursiveElementVisitor() {
        override fun visitClass(aClass: PsiClass) {
            stats.classNumber++
            super.visitClass(aClass)
        }

        override fun visitMethod(method: PsiMethod) {
            stats.methodNumber++
            super.visitMethod(method)
        }
    }
}