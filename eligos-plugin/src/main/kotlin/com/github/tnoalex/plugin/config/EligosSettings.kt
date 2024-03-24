package com.github.tnoalex.plugin.config

import com.github.tnoalex.formatter.FormatterTypeEnum
import com.intellij.openapi.components.*
import com.intellij.openapi.project.Project

@State(name = "EligosSettings", storages = [Storage("eligos.xml")])
@Service(Service.Level.PROJECT)
internal class EligosSettings(private val project: Project) :
    SimplePersistentStateComponent<EligosSettings.EligosState>(EligosState()) {

    fun getFormatType(): FormatterTypeEnum {
        return state.formatType
    }

    fun setFormatType(typeEnum: FormatterTypeEnum?) {
        typeEnum?.let { state.formatType = it }
    }

    fun getOutputPath(): String {
        return state.resultOutputPath ?: project.basePath!!
    }

    fun setOutputPath(path: String) {
        state.resultOutputPath = path
    }

    companion object {
        fun getInstance(project: Project): EligosSettings = project.service()
    }

    class EligosState : BaseState() {
        var formatType by enum<FormatterTypeEnum>(FormatterTypeEnum.JSON)

        var resultOutputPath by string(null)
    }
}

