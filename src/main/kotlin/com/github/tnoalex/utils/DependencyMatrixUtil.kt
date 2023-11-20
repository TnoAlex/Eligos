package com.github.tnoalex.utils

import com.github.tnoalex.foundation.algorithm.AdjacencyList
import com.github.tnoalex.foundation.algorithm.DuplicateEdgeStrategy
import depends.matrix.core.DependencyMatrix


fun DependencyMatrix.toAdjacencyList(): AdjacencyList<Int> {
    val adjacencyList = AdjacencyList<Int>()
    for (i in 0..<nodes.size) {
        adjacencyList.addNode(i)
    }
    dependencyPairs.forEach {
        it.dependencies.forEach { dep ->
            adjacencyList.addArc(it.from, it.to, dep.type, DuplicateEdgeStrategy.APPEND)
        }
    }
    return adjacencyList
}