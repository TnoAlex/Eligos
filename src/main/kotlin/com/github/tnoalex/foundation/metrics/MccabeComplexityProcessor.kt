package com.github.tnoalex.foundation.metrics

abstract class MccabeComplexityProcessor {
    private val functionMap = HashMap<String, ArrayList<Int>>()

    private val terminatedMap = HashSet<String>()
    abstract fun hookAst()
    abstract fun processFile(fullFileName: String)

    fun getMccabeComplex(): Map<String, Int> {
        return functionMap.map {
            it.key to it.value[ARC_INDEX] - it.value[NODE_INDEX] + 2
        }.toMap()
    }

    protected fun finishProcess() {
        functionMap.clear()
        terminatedMap.clear()
    }

    protected fun addFunction(functionId: String) {
        functionMap[functionId] = ArrayList(2)
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