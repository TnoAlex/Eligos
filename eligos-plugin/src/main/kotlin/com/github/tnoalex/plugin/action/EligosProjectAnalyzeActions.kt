package com.github.tnoalex.plugin.action

import com.github.tnoalex.Analyzer
import com.github.tnoalex.config.ConfigParser
import com.github.tnoalex.formatter.json.JsonFormatter
import com.github.tnoalex.foundation.ApplicationContext
import com.github.tnoalex.foundation.LaunchEnvironment
import com.github.tnoalex.foundation.bean.container.SimpleSingletonBeanContainer
import com.github.tnoalex.plugin.bean.IdeBeanSupportStructureScanner
import com.github.tnoalex.plugin.parser.IdePluginFileDistributor
import com.intellij.ide.plugins.PluginManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project


class EligosProjectAnalyzeActions : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val backgroundTask = object : Task.Backgroundable(project, "Eligos analyzing", true, ALWAYS_BACKGROUND) {
            override fun run(indicator: ProgressIndicator) {
                indicator.text = "Running Eligos tasks..."
                indicator.isIndeterminate = true
                ApplicationManager.getApplication().runReadAction { startEligosTask(project) }
            }
        }
        ProgressManager.getInstance().run(backgroundTask)
    }

    private fun startEligosTask(project: Project) {
        initEligosApplication()
        val idePluginFileDistributor = ApplicationContext.getExactBean(IdePluginFileDistributor::class.java) ?: return
        idePluginFileDistributor.initPsiManager(project)
        Analyzer(JsonFormatter(), listOf("java", "kotlin"), LaunchEnvironment.IDE_PLUGIN).analyze()
    }

    private fun initEligosApplication() {
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
    }
}