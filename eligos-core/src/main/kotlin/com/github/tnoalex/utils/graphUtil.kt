package com.github.tnoalex.utils

import org.jgrapht.Graph

fun <V : Any, T> Graph<V, T>.toAdjacencyMatrices(): Pair<HashMap<V, Int>, ArrayList<ArrayList<Int>>> {
    val nodeMap = HashMap<V, Int>()
    var nodeCounter = 0
    vertexSet().forEach {
        nodeMap[it] = nodeCounter
        nodeCounter++
    }
    val matrix = let {
        val list = ArrayList<ArrayList<Int>>()
        it.vertexSet().forEach { _ ->
            val subList = ArrayList<Int>(it.vertexSet().size).apply { repeat(it.vertexSet().size) { add(0) } }
            list.add(subList)
        }
        list
    }
    edgeSet().forEach {
        val target = getEdgeTarget(it)
        val source = getEdgeSource(it)
        val row = nodeMap[source]!!
        val col = nodeMap[target]!!
        matrix[row][col] = 1
    }
    return Pair(nodeMap, matrix)
}