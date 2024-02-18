package com.github.tnoalex.processor.common

import com.github.tnoalex.AnalysisHierarchyEnum
import com.github.tnoalex.Context
import com.github.tnoalex.config.WiredConfig
import com.github.tnoalex.events.EntityRepoFinishedEvent
import com.github.tnoalex.foundation.bean.Component
import com.github.tnoalex.foundation.eventbus.EventListener
import com.github.tnoalex.issues.ExcessiveParamsIssue
import com.github.tnoalex.processor.AstProcessor
import com.github.tnoalex.utils.getEntitiesByType
import depends.entity.FunctionEntity
import java.util.*

@Component
class TooManyParametersProcessor : AstProcessor {
    private val issues = LinkedList<ExcessiveParamsIssue>()

    @WiredConfig("function.arity")
    private var arity: Int = 0

    override val order: Int
        get() = Short.MAX_VALUE.toInt()

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
            it.parameters.size > arity
        }.forEach {
            val file = functionDependency!!.nodes.first { f ->
                f.split("(")[1] == it.qualifiedName + ")"
            }.split("(")[0]
            val params = HashMap<String, String>()
            it.parameters.forEach { p ->
                if (p.rawType == null) return@forEach
                params[p.rawName.name] = p.rawType.name
            }
            issues.add(ExcessiveParamsIssue(file, it.qualifiedName.split(".").last(), params))
        }
    }
}