package com.github.tnoalex.algorithm

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach

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
        println(res)
        assertArrayEquals(
            arrayOf(
                'a', 'f', 'g', 'b', 'c', 'e', 'd'
            ), res.toArray()
        )
    }
}