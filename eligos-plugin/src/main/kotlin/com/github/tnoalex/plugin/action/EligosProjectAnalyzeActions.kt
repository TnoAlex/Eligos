package com.github.tnoalex.plugin.action

import com.github.tnoalex.Analyzer
import com.github.tnoalex.config.ConfigParser
import com.github.tnoalex.formatter.FormatterTypeEnum
import com.github.tnoalex.formatter.Reporter
import com.github.tnoalex.foundation.ApplicationContext
import com.github.tnoalex.foundation.LaunchEnvironment
import com.github.tnoalex.foundation.bean.container.SimpleSingletonBeanContainer
import com.github.tnoalex.plugin.bean.IdeBeanSupportStructureScanner
import com.github.tnoalex.plugin.parser.IdePluginFileDistributor
import com.github.tnoalex.specs.AnalyzerSpec
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
import java.io.File


class EligosProjectAnalyzeActions : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val analysisBackgroundTask =
            object : Task.Backgroundable(project, "Eligos analyzing", true, ALWAYS_BACKGROUND) {
                override fun run(indicator: ProgressIndicator) {
                    indicator.text = "Running Eligos analysis task..."
                    indicator.isIndeterminate = true
                    ApplicationManager.getApplication().runReadAction { startEligosAnalyzerTask(project) }
                }
            }
        val reportBackgroundTask = object : Task.Backgroundable(project, "Eligos reporting", false, ALWAYS_BACKGROUND) {
            override fun run(indicator: ProgressIndicator) {
                indicator.text = "Eligos reporting..."
                indicator.isIndeterminate = true
                ApplicationManager.getApplication().runWriteAction { startEligosReportTask(project) }
            }
        }
        ProgressManager.getInstance().run(analysisBackgroundTask)
    }

    private fun startEligosReportTask(project: Project) {
        val formatterSpec = ApplicationContext.getExactBean(Analyzer::class.java)!!.analyzerSpec.formatterSpec
        Reporter(formatterSpec).report()
        ApplicationManager.getApplication().invokeLater {
            displayDoneBalloon(project)
        }
    }

    private fun startEligosAnalyzerTask(project: Project) {
        if (!ApplicationContext.isInitialized) {
            initEligosApplication(project)
            val idePluginFileDistributor =
                ApplicationContext.getExactBean(IdePluginFileDistributor::class.java) ?: return
            idePluginFileDistributor.initPsiManager(project)
        }
        try {
            ApplicationContext.getExactBean(Analyzer::class.java)!!.analyze()
            startEligosReportTask(project)
        } catch (e: Exception) {
            displayErrorBalloon(project)
            throw e
        }
    }

    private fun initEligosApplication(project: Project) {
        val classLoader = PluginManager.getPlugins().first { it.name == "Eligos" }.pluginClassLoader
            ?: throw RuntimeException("Can not find plugin clas loader")
        val ideBeanSupportStructureScanner = IdeBeanSupportStructureScanner(classLoader)
        ApplicationContext.addBeanRegisterDistributor(listOf(ideBeanSupportStructureScanner))
        ApplicationContext.addBeanContainerScanner(listOf(ideBeanSupportStructureScanner))
        ApplicationContext.addBeanHandlerScanner(listOf(ideBeanSupportStructureScanner))
        val configParser = ConfigParser()
        ApplicationContext.addBean(configParser::class.java.simpleName, configParser, SimpleSingletonBeanContainer)

        ApplicationContext.currentClassLoader = classLoader
        ApplicationContext.init()
        val analyzer = createAnalyzer(project)
        ApplicationContext.addBean(analyzer::class.java.simpleName, analyzer, SimpleSingletonBeanContainer)
    }

    private fun createAnalyzer(project: Project): Analyzer {
        return Analyzer(
            AnalyzerSpec(
                "kotlin",
                "java",
                extendRulePath = project.basePath?.let { File(it) },
                kotlinCompilerSpec = null,
                formatterSpec = FormatterSpec(
                    project.basePath ?: "",
                    project.basePath?.let { File(it).toPath() } ?: File(".").toPath(),
                    project.name,
                    FormatterTypeEnum.JSON,
                ),
                launchEnvironment = LaunchEnvironment.IDE_PLUGIN
            )
        )
    }

    private fun displayErrorBalloon(project: Project) {
        NotificationGroupManager.getInstance().getNotificationGroup("Eligos Error")
            .createNotification(
                "Eligos error",
                "An error has occurred ... please contact us on github",
                NotificationType.ERROR
            )
            .notify(project)
    }

    private fun displayDoneBalloon(project: Project) {
        NotificationGroupManager.getInstance().getNotificationGroup("Eligos Done")
            .createNotification("Eligos done!", NotificationType.INFORMATION)
            .notify(project)
    }
}