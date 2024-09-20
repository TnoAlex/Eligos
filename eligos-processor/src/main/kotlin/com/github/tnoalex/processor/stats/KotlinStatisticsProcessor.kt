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
import com.github.tnoalex.statistics.KotlinStatistics
import com.intellij.psi.PsiFile
import org.jetbrains.kotlin.psi.*


@Component(order = -1)
@Suitable(LaunchEnvironment.CLI)
class KotlinStatisticsProcessor : PsiProcessor {
    private var stats = KotlinStatistics()
    override val supportLanguage: List<Language>
        get() = listOf(KotlinLanguage)

    @EventListener(order = -1, filterClazz = [KtFile::class])
    override fun process(psiFile: PsiFile) {
        psiFile as KtFile
        stats.fileNumber++
        stats.lineNumber += psiFile.lineCount
        psiFile.accept(ktVisitor)
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