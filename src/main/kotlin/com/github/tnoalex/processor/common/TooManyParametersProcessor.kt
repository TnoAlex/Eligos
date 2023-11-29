package com.github.tnoalex.processor.common

import com.github.tnoalex.AnalysisHierarchyEnum
import com.github.tnoalex.Context
import com.github.tnoalex.events.EntityRepoFinishedEvent
import com.github.tnoalex.foundation.eventbus.EventListener
import com.github.tnoalex.issues.ExcessiveParamsIssue
import com.github.tnoalex.processor.AstProcessor
import com.github.tnoalex.rules.FunctionRule
import com.github.tnoalex.rules.RuleContainer
import com.github.tnoalex.utils.getEntitiesByType
import depends.entity.FunctionEntity
import java.util.*

class TooManyParametersProcessor : AstProcessor {
    private val issues = LinkedList<ExcessiveParamsIssue>()

    @EventListener
    fun process(event: EntityRepoFinishedEvent) {
        findTooManyParameters(event.source as Context)
        (event.source as Context).reportIssues(issues)
        issues.clear()
    }

    private fun findTooManyParameters(context: Context) {
        val functionEntities = context.getRepo().getEntitiesByType(FunctionEntity::class.java)
        val functionDependency = context.getDependencyMatrix(AnalysisHierarchyEnum.METHOD)
        functionEntities.map { it as FunctionEntity }.filter {
            it.parameters.size > (RuleContainer.getByType(FunctionRule::class) as FunctionRule).arity
        }.forEach {
            val file = functionDependency!!.nodes.first { f ->
                f.split("(")[1] == it.qualifiedName + ")"
            }.split("(")[0]
            issues.add(ExcessiveParamsIssue(file, it.qualifiedName.split(".").last(), it.parameters.size))
        }
    }
}