package com.github.tnoalex.processor.common

import com.github.tnoalex.AnalysisHierarchyEnum
import com.github.tnoalex.Context
import com.github.tnoalex.config.ConfigContainer
import com.github.tnoalex.config.FunctionConfig
import com.github.tnoalex.events.EntityRepoFinishedEvent
import com.github.tnoalex.foundation.eventbus.EventListener
import com.github.tnoalex.issues.ExcessiveParamsIssue
import com.github.tnoalex.processor.AstProcessor
import com.github.tnoalex.utils.getEntitiesByType
import depends.entity.FunctionEntity
import java.util.*

class TooManyParametersProcessor : AstProcessor {
    private val issues = LinkedList<ExcessiveParamsIssue>()

    override val order: Int
        get() = -1

    @EventListener
    fun process(event: EntityRepoFinishedEvent) {
        if (event.checkType(Context::class)) {
            findTooManyParameters(event.source as Context)
            (event.source as Context).reportIssues(issues)
            issues.clear()
        }
    }

    private fun findTooManyParameters(context: Context) {
        val functionEntities = context.getRepo().getEntitiesByType(FunctionEntity::class.java)
        val functionDependency = context.getDependencyMatrix(AnalysisHierarchyEnum.METHOD)
        functionEntities.map { it as FunctionEntity }.filter {
            it.parameters.size > (ConfigContainer.getByType(FunctionConfig::class) as FunctionConfig).arity
        }.forEach {
            val file = functionDependency!!.nodes.first { f ->
                f.split("(")[1] == it.qualifiedName + ")"
            }.split("(")[0]
            issues.add(ExcessiveParamsIssue(file, it.qualifiedName.split(".").last(), it.parameters.size))
        }
    }
}