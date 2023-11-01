package com.github.tnoalex.utils

import com.github.tnoalex.algorithm.AdjacencyList
import com.github.tnoalex.entity.enums.DuplicateEdgeStrategy
import depends.matrix.core.DependencyMatrix


fun DependencyMatrix.toAdjacencyList(): AdjacencyList<Int> {
    val adjacencyList = AdjacencyList<Int>()
    for (i in 0 until nodes.size) {
        adjacencyList.addNode(i)
    }
    dependencyPairs.forEach {
        it.dependencies.forEach { dep ->
            adjacencyList.addArc(it.from, it.to, dep.type, DuplicateEdgeStrategy.APPEND)
        }
    }
    return adjacencyList
}