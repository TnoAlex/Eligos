package com.github.tnoalex.processor.stats

import com.github.tnoalex.events.AllFileParsedEvent
import com.github.tnoalex.foundation.LaunchEnvironment
import com.github.tnoalex.foundation.bean.Component
import com.github.tnoalex.foundation.bean.Suitable
import com.github.tnoalex.foundation.eventbus.EventListener
import com.github.tnoalex.foundation.language.JavaLanguage
import com.github.tnoalex.foundation.language.KotlinLanguage
import com.github.tnoalex.foundation.language.Language
import com.github.tnoalex.processor.PsiProcessor
import com.github.tnoalex.processor.utils.lineCount
import com.github.tnoalex.statistics.JavaStatistics
import com.intellij.psi.JavaRecursiveElementVisitor
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.PsiMethod

@Component(order = -1)
@Suitable(LaunchEnvironment.CLI)
class JavaStatisticsProcessor : PsiProcessor {
    private var stats = JavaStatistics()
    override val supportLanguage: List<Language>
        get() = listOf(JavaLanguage)

    @EventListener(order = -1, filterClazz = [PsiJavaFile::class])
    override fun process(psiFile: PsiFile) {
        psiFile as PsiJavaFile
        stats.fileNumber++
        stats.lineNumber += psiFile.lineCount
        psiFile.accept(javaVisitor)
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