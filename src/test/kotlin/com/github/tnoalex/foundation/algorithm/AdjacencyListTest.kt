package com.github.tnoalex.foundation.algorithm

import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class AdjacencyListTest {

    private val graph = AdjacencyList<Char>()

    @BeforeEach
    fun initGraph() {
        for (i in 'a'..'g') {
            graph.addNode(i)
        }
        graph.addArc('a', 'b')
        graph.addArc('b', 'c')
        graph.addArc('c', 'd')
        graph.addArc('c', 'e')
        graph.addArc('d', 'e')
        graph.addArc('d', 'b')
        graph.addArc('a', 'f')
        graph.addArc('f', 'g')
        graph.addArc('g', 'a')
    }

    @Test
    fun testSolveSCC() {
        val scc = graph.solveSCC()
        assertArrayEquals(arrayOf(listOf(4), listOf(1, 2, 3), listOf(0, 5, 6)), scc.toTypedArray())
    }

    @Test
    fun testBreadthFirstTraversal() {
        val res = graph.breadthFirstTraversal()
        assertArrayEquals(
            arrayOf(
                'a', 'f', 'b', 'g', 'c', 'e', 'd'
            ), res.toArray()
        )
    }

    @Test
    fun depthFirstTraversal() {
        val res = graph.depthFirstTraversal()
        assertArrayEquals(
            arrayOf(
                'a', 'f', 'g', 'b', 'c', 'e', 'd'
            ), res.toArray()
        )
    }

    @Test
    fun testSubPartOfNodes() {
        val res = graph.subPartOfNodes(listOf(0, 1, 2))
        val accGraph = AdjacencyList<Char>()
        for (i in 'a'..'c') {
            accGraph.addNode(i)
        }
        accGraph.addArc('a', 'b')
        accGraph.addArc('b', 'c')
        assertArrayEquals(accGraph.depthFirstTraversal().toArray(), res.depthFirstTraversal().toArray())
    }
}