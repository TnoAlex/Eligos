package com.github.tnoalex.processor.common

import com.github.tnoalex.config.InjectConfig
import com.github.tnoalex.foundation.LaunchEnvironment
import com.github.tnoalex.foundation.bean.Component
import com.github.tnoalex.foundation.bean.Suitable
import com.github.tnoalex.foundation.bean.inject.InjectBean
import com.github.tnoalex.foundation.eventbus.EventListener
import com.github.tnoalex.foundation.language.JavaLanguage
import com.github.tnoalex.foundation.language.KotlinLanguage
import com.github.tnoalex.foundation.language.Language
import com.github.tnoalex.issues.Severity
import com.github.tnoalex.issues.common.ExcessiveParamsIssue
import com.github.tnoalex.processor.IssueProcessor
import com.github.tnoalex.processor.ShareSpace
import com.github.tnoalex.processor.common.providers.TooManyParametersProcessorProvider
import com.github.tnoalex.processor.utils.filePath
import com.github.tnoalex.processor.utils.nameCanNotResolveWarn
import com.github.tnoalex.processor.utils.startLine
import com.intellij.psi.*
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtTreeVisitorVoid
import org.slf4j.LoggerFactory
import java.util.*

@Component
@Suitable(LaunchEnvironment.CLI)
class TooManyParametersProcessor : AbstractCommonProcessor() {
    override val severity: Severity
        get() = Severity.CODE_SMELL
    override val supportLanguage: List<Language>
        get() = listOf(JavaLanguage, KotlinLanguage)

    @InjectConfig("function.arity")
    private var arity: Int = 0

    private val issues = LinkedList<ExcessiveParamsIssue>()

    private val myShareSpace = TooManyParametersShareSpace()

    @InjectBean(beanType = TooManyParametersProcessorProvider::class)
    override lateinit var processorProvider: AbstractSpecificProcessorProvider

    @EventListener(filterClazz = [PsiJavaFile::class, KtFile::class])
    override fun process(psiFile: PsiFile) {
        invokeSpecificProcessor(psiFile)
        context.reportIssues(issues)
        issues.clear()
    }

    override fun createShearSpace(): ShareSpace = myShareSpace

    internal inner class TooManyParametersShareSpace : ShareSpace {
        internal val shareIssues: MutableList<ExcessiveParamsIssue>
            get() = issues
        internal val shareArity: Int
            get() = arity
    }
}