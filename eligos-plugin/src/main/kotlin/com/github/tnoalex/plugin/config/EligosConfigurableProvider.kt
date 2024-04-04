package com.github.tnoalex.plugin.config

import com.github.tnoalex.formatter.FormatterTypeEnum
import com.github.tnoalex.issues.Severity
import com.github.tnoalex.plugin.EligosBundle
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.BoundConfigurable
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.ConfigurableProvider
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.dsl.builder.bindItem
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.columns
import com.intellij.ui.dsl.builder.panel

internal class EligosConfigurableProvider(private val project: Project) : ConfigurableProvider() {
    override fun createConfigurable(): Configurable {
        return EligosSettingsPanel(project)
    }
}

internal class EligosSettingsPanel(private val project: Project) :
    BoundConfigurable(EligosBundle.message("eligos.name")) {
    private val settings = EligosSettings.getInstance(project)
    override fun createPanel(): DialogPanel = panel {
        row(EligosBundle.message("eligos.settings.formatLabel")) {
            comboBox(FormatterTypeEnum.entries)
                .bindItem(settings::getFormatType, settings::setFormatType)
        }
        row(EligosBundle.message("eligos.setting.severityLevel")) {
            comboBox(Severity.entries)
                .bindItem(settings::getSeverityLevel, settings::setSeverityLevel)
        }
        row(EligosBundle.message("eligos.settings.outputPathLabel")) {
            textFieldWithBrowseButton(
                fileChooserDescriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor(),
                project = project
            ).bindText(settings::getOutputPath, settings::setOutputPath)
                .columns(45)
                .resizableColumn()
        }
    }
}