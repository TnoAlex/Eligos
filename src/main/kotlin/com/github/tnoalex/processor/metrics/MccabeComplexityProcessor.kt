package com.github.tnoalex.processor.metrics

import com.github.tnoalex.config.ConfigContainer
import com.github.tnoalex.config.FunctionConfig
import com.github.tnoalex.elements.FileElement
import com.github.tnoalex.events.FileExitEvent
import com.github.tnoalex.foundation.eventbus.EventListener
import com.github.tnoalex.issues.ComplexMethodIssue
import com.github.tnoalex.processor.ContextAstProcessor
import java.util.*

abstract class MccabeComplexityProcessor : ContextAstProcessor() {
    private val functionMap = HashMap<String, ArrayList<Int>>()
    private val closureFunctionMap = HashMap<String, ArrayList<String>>()

    private val terminatedMap = HashSet<String>()

    override val order: Int
        get() = Short.MAX_VALUE.toInt()

    @EventListener
    fun report(event: FileExitEvent) {
        val issues = LinkedList<ComplexMethodIssue>()
        getMccabeComplex().filterValues {
            it >= (ConfigContainer.getByType(FunctionConfig::class) as FunctionConfig).maxCyclomaticComplexity
        }.forEach { (k, v) ->
            issues.add(
                ComplexMethodIssue(
                    (event.source as FileElement).elementName, k, v
                )
            )
        }
        context.reportIssues(issues)
        finishProcess()
    }

    fun getMccabeComplex(): Map<String, Int> {
        return margeFunction(functionMap.map { it.key to it.value[ARC_INDEX] - it.value[NODE_INDEX] + 2 }.toMap())
            .map { it.key.split(":")[0] to it.value }.toMap()
    }

    private fun margeFunction(functionMap: Map<String, Int>): HashMap<String, Int> {
        val res = HashMap<String, Int>()
        functionMap.forEach { (k, v) ->
            if (hasParentFunction(k)) {
                return@forEach // Closure functions don't need to be split
            }
            var complexity = v
            getClosureFunctions(k).forEach {
                complexity += functionMap[it]!!
            }
            res[k] = complexity
        }
        return res
    }

    private fun hasParentFunction(functionId: String): Boolean {
        closureFunctionMap.values.forEach {
            if (it.contains(functionId))
                return true
        }
        return false
    }

    private fun getClosureFunctions(parentFunction: String): MutableList<String> {
        val childFunctions = mutableListOf<String>()

        if (parentFunction in closureFunctionMap.keys) {
            childFunctions.addAll(closureFunctionMap[parentFunction]!!)
            for (childFunction in closureFunctionMap[parentFunction]!!) {
                childFunctions.addAll(getClosureFunctions(childFunction))
            }
        }

        return childFunctions
    }

    open fun finishProcess() {
        functionMap.clear()
        terminatedMap.clear()
        closureFunctionMap.clear()
    }

    protected fun recordClosurePair(parent: String, current: String) {
        if (closureFunctionMap[parent] == null) {
            closureFunctionMap[parent] = arrayListOf(current)
        } else {
            closureFunctionMap[parent]!!.add(current)
        }
    }

    protected fun addFunction(functionId: String) {
        if (functionMap[functionId] == null) {
            functionMap[functionId] = arrayListOf(1, 1)
        }
    }

    protected fun addTerminatedNode(functionId: String) {
        if (terminatedMap.contains(functionId)) {
            return
        } else {
            terminatedMap.add(functionId)
            addNode(functionId)
        }
    }

    protected fun addNode(functionId: String, nodeNums: Int = 1) {
        functionMap[functionId] ?: throw RuntimeException("Unexpected keys:${functionId}")
        functionMap[functionId]!![NODE_INDEX] += nodeNums
    }

    protected fun addArc(functionId: String, arcNums: Int = 1) {
        functionMap[functionId] ?: throw RuntimeException("Unexpected keys:${functionId}")
        functionMap[functionId]!![ARC_INDEX] += arcNums
    }

    companion object {
        private const val NODE_INDEX = 0
        private const val ARC_INDEX = 1
    }
}