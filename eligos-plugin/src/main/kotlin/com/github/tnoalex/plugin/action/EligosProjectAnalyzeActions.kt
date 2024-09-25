package com.github.tnoalex.plugin.action

import com.github.tnoalex.Analyzer
import com.github.tnoalex.config.ConfigParser
import com.github.tnoalex.formatter.FormatterTypeEnum
import com.github.tnoalex.formatter.Reporter
import com.github.tnoalex.foundation.ApplicationContext
import com.github.tnoalex.foundation.LaunchEnvironment
import com.github.tnoalex.foundation.bean.container.SimpleSingletonBeanContainer
import com.github.tnoalex.plugin.EligosBundle
import com.github.tnoalex.plugin.bean.IdeBeanSupportStructureScanner
import com.github.tnoalex.plugin.config.EligosSettings
import com.github.tnoalex.plugin.parser.IdePluginFileDistributor
import com.github.tnoalex.specs.AnalyzerSpec
import com.github.tnoalex.specs.DebugSpec
import com.github.tnoalex.specs.FormatterSpec
import com.intellij.ide.plugins.PluginManager
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFileManager
import org.jetbrains.kotlin.config.ApiVersion
import org.jetbrains.kotlin.config.LanguageVersion
import org.jetbrains.kotlin.config.LanguageVersionSettings
import org.jetbrains.kotlin.config.LanguageVersionSettingsImpl
import org.jetbrains.kotlin.resolve.calls.smartcasts.DataFlowValueFactory
import org.jetbrains.kotlin.resolve.calls.smartcasts.DataFlowValueFactoryImpl
import java.io.File


class EligosProjectAnalyzeActions : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val analysisBackgroundTask =
            object :
                Task.Backgroundable(project, EligosBundle.message("eligos.analysis.title"), true, ALWAYS_BACKGROUND) {
                override fun run(indicator: ProgressIndicator) {
                    indicator.text = EligosBundle.message("eligos.analysis.message")
                    indicator.isIndeterminate = true
                    ApplicationManager.getApplication().runReadAction { startEligosAnalyzerTask(project) }
                }
            }
        ProgressManager.getInstance().run(analysisBackgroundTask)
    }

    private fun startEligosReportTask(project: Project) {
        val settings = project.getService(EligosSettings::class.java)!!
        val formatterSpec = FormatterSpec(
            settings.getOutputPath(),
            File(settings.getOutputPath()).toPath(),
            project.name,
            settings.getFormatType()
        )
        Reporter(formatterSpec).report()
        ApplicationManager.getApplication().invokeLater {
            displayDoneBalloon(project)
            VirtualFileManager.getInstance().refreshWithoutFileWatcher(false)
        }
    }

    private fun startEligosAnalyzerTask(project: Project) {
        if (!ApplicationContext.isInitialized) {
            initEligosApplication(project)
        }
        val idePluginFileDistributor =
            ApplicationContext.getExactBean(IdePluginFileDistributor::class.java) ?: return
        idePluginFileDistributor.initPsiManager(project)
        try {
            ApplicationContext.getExactBean(Analyzer::class.java)!!.analyze()
            startEligosReportTask(project)
        } catch (e: Exception) {
            displayErrorBalloon(project)
            throw e
        }
    }

    private fun initEligosApplication(project: Project) {
        val classLoader =
            PluginManager.getPlugins().first { it.name == EligosBundle.message("eligos.name") }.pluginClassLoader
                ?: throw RuntimeException(EligosBundle.message("eligos.classloader.error.message"))
        val ideBeanSupportStructureScanner = IdeBeanSupportStructureScanner(classLoader)
        ApplicationContext.addBeanRegisterDistributor(listOf(ideBeanSupportStructureScanner))
        ApplicationContext.addBeanContainerScanner(listOf(ideBeanSupportStructureScanner))
        ApplicationContext.addBeanHandlerScanner(listOf(ideBeanSupportStructureScanner))
        val configParser = ConfigParser()
        ApplicationContext.addBean(configParser::class.java.simpleName, configParser, SimpleSingletonBeanContainer)

        ApplicationContext.addBean(
            DataFlowValueFactory::class.java.simpleName,
            createDataFlowFactory(),
            SimpleSingletonBeanContainer
        )

        ApplicationContext.currentClassLoader = classLoader
        ApplicationContext.init()
        val analyzer = createAnalyzer(project)
        ApplicationContext.addBean(analyzer::class.java.simpleName, analyzer, SimpleSingletonBeanContainer)
    }

    private fun createDataFlowFactory(): DataFlowValueFactoryImpl {
        val languageVersionSettings: LanguageVersionSettings =
            LanguageVersion.fromVersionString(EligosBundle.message("eligos.supportKtVersion"))!!.let {
                LanguageVersionSettingsImpl(
                    languageVersion = it,
                    apiVersion = ApiVersion.createByLanguageVersion(it)
                )
            }
        return DataFlowValueFactoryImpl(languageVersionSettings)
    }

    private fun createAnalyzer(project: Project): Analyzer {
        val settings = project.getService(EligosSettings::class.java)!!
        return Analyzer(
            AnalyzerSpec(
                "kotlin",
                "java",
                extendRulePath = project.basePath?.let { File(it) },
                kotlinCompilerSpec = null,
                formatterSpec = FormatterSpec(
                    settings.getOutputPath(),
                    File(settings.getOutputPath()).toPath(),
                    project.name,
                    FormatterTypeEnum.JSON,
                ),
                launchEnvironment = LaunchEnvironment.IDE_PLUGIN,
                severityLevel = settings.getSeverityLevel(),
                confidenceLevel = settings.state.confidenceLevel,
                debugSpec = DebugSpec()
            )
        )
    }

    private fun displayErrorBalloon(project: Project) {
        NotificationGroupManager.getInstance()
            .getNotificationGroup(EligosBundle.message("eligos.errorNotification.group"))
            .createNotification(
                EligosBundle.message("eligos.errorNotification.title"),
                EligosBundle.message("eligos.errorNotification.message"),
                NotificationType.ERROR
            )
            .notify(project)
    }

    private fun displayDoneBalloon(project: Project) {
        NotificationGroupManager.getInstance()
            .getNotificationGroup(EligosBundle.message("eligos.doneNotification.group"))
            .createNotification(EligosBundle.message("eligos.doneNotification.title"), NotificationType.INFORMATION)
            .notify(project)
    }
}