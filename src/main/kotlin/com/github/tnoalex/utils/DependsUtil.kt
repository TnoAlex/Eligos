package com.github.tnoalex.utils

import com.github.tnoalex.foundation.algorithm.AdjacencyList
import com.github.tnoalex.foundation.algorithm.DuplicateEdgeStrategy
import depends.entity.Entity
import depends.matrix.core.DependencyMatrix
import depends.matrix.core.DependencyPair


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

fun DependencyMatrix.getDependencyPairByType(dependencyType: String): List<DependencyPair> {
    return dependencyPairs.filter { it.dependencies.find { d -> d.type == dependencyType } != null }
}

fun Entity.getTargetTypeParent(type :Class<*>): Entity? {
    var parent = parent
    while (parent!=null){
        if (parent::class.java == type){
            return parent
        }
        parent = parent.parent
    }
    return null
}