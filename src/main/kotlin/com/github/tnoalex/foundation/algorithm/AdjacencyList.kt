package com.github.tnoalex.foundation.algorithm

import com.github.tnoalex.entity.enums.DuplicateEdgeStrategy
import com.github.tnoalex.entity.enums.DuplicateEdgeStrategy.APPEND
import com.github.tnoalex.entity.enums.DuplicateEdgeStrategy.DISCARD
import com.github.tnoalex.utils.encodeBySHA1
import java.util.*
import kotlin.math.min


class AdjacencyList<T : Any> {
    private val nodesArray: ArrayList<AdjacencyHead<T>> = ArrayList()
    var nodeNum: Int = 0
        private set

    var arcNum: Int = 0
        private set

    val identifier by lazy {
        generateId()
    }

    fun addNodes(nodes: Collection<T>) {
        nodes.forEach {
            addNode(it)
        }
    }

    fun addNode(node: T) {
        nodesArray.add(AdjacencyHead(node, null, nodeNum))
        nodeNum++
    }

    fun addArc(from: T, to: T, info: Any?, strategy: DuplicateEdgeStrategy) {
        val fromNode = locatingNode(from)
        val toNode = locatingNode(to)

        fun action(act: (AdjacencyArc<T>) -> Unit) {
            val newArc = AdjacencyArc(toNode, nodesArray[fromNode].firstArc)
            val oldArc = newArc.nextArc
            nodesArray[fromNode].firstArc = newArc
            oldArc?.nextArc = null
            arcNum++
            act(newArc)
        }
        if (info == null) {
            if (isExistArc(fromNode, toNode)) return
        } else {
            if (isExistArc(fromNode, toNode)) {
                when (strategy) {
                    APPEND -> {
                        val arc = getArcBetween(fromNode, toNode)
                        arc.info.add(info)
                        return
                    }

                    DISCARD -> return
                    else -> {
                        action { it.info.add(info) }
                    }
                }
            }
        }
        action {}
    }


    fun addArc(from: T, to: T) {
        addArc(from, to, null, DISCARD)
    }

    fun isExistArc(from: T, to: T): Boolean {
        return isExistArc(locatingNode(from), locatingNode(to))
    }

    private fun isExistArc(from: Int, to: Int): Boolean {
        var arc: AdjacencyArc<T>? = nodesArray[from].firstArc ?: return false
        while (arc != null) {
            if (arc.adjVex == to)
                return true
            arc = arc.nextArc
        }
        return false
    }

    fun getArcInfoBetween(from: T, to: T): LinkedList<Any> {
        return getArcInfoBetween(locatingNode(from), locatingNode(to))
    }

    private fun getArcInfoBetween(from: Int, to: Int): LinkedList<Any> {
        return getArcBetween(from, to).info
    }

    private fun getArcBetween(from: Int, to: Int): AdjacencyArc<T> {
        var arc: AdjacencyArc<T>? =
            nodesArray[from].firstArc ?: throw RuntimeException("Unanticipated indexes exception")
        while (arc != null && arc.adjVex != to) {
            arc = arc.nextArc
        }
        return arc ?: throw RuntimeException("Unanticipated indexes exception")
    }

    fun depthFirstTraversal(): ArrayList<T> {
        val visited = BooleanArray(nodeNum)
        val res = ArrayList<T>()
        fun dfs(vertex: Int, visited: BooleanArray) {
            visited[vertex] = true
            val vHead = nodesArray[vertex]
            res.add(vHead.data)

            var arcNode = vHead.firstArc
            while (arcNode != null) {
                if (!visited[arcNode.adjVex]) {
                    dfs(arcNode.adjVex, visited)
                }
                arcNode = arcNode.nextArc
            }
        }
        for (i in 0..<nodeNum) {
            if (!visited[i]) {
                dfs(i, visited)
            }
        }
        return res
    }

    fun breadthFirstTraversal(): ArrayList<T> {
        val visited = BooleanArray(nodeNum)
        val queue: Queue<Int> = LinkedList()

        val res = ArrayList<T>()

        for (i in 0..<nodeNum) {
            if (!visited[i]) {
                queue.offer(i)
                visited[i] = true

                while (!queue.isEmpty()) {
                    val vertex = queue.poll()
                    val vHead = nodesArray[vertex]
                    res.add(vHead.data)

                    var arcNode = vHead.firstArc
                    while (arcNode != null) {
                        if (!visited[arcNode.adjVex]) {
                            queue.offer(arcNode.adjVex)
                            visited[arcNode.adjVex] = true
                        }
                        arcNode = arcNode.nextArc
                    }
                }
            }
        }
        return res
    }

    fun subPartOfNodes(nodes: List<Int>): AdjacencyList<T> {
        val subPart = AdjacencyList<T>()
        nodes.forEach {
            subPart.addNode(nodesArray[it].data)
        }
        for (i in nodes) {
            for (j in nodes) {
                if (isExistArc(i, j)) {
                    val info = getArcInfoBetween(i, j)
                    subPart.addArc(nodesArray[i].data, nodesArray[j].data)
                    if (!info.isEmpty()) {
                        info.forEach {
                            subPart.addArc(nodesArray[i].data, nodesArray[j].data, it, APPEND)
                        }
                    }
                }
            }
        }
        return subPart
    }


    fun solveSCC(): List<List<Int>> {
        val dfn = IntArray(nodeNum) { -1 }
        val low = IntArray(nodeNum) { -1 }
        val visited = IntArray(nodeNum) { -1 }
        var index = 0
        val inStack = BooleanArray(nodeNum) { false }
        val stack = Stack<Int>()
        var cnt = 0

        fun tarjan(pos: Int) {
            var v: Int
            dfn[pos] = ++index
            low[pos] = dfn[pos]
            stack.push(pos)
            inStack[pos] = true

            var edge = nodesArray[pos].firstArc
            while (edge != null) {
                v = edge.adjVex
                if (dfn[v] == -1) {
                    tarjan(v)
                    low[pos] = min(low[pos], low[v])
                } else if (inStack[v]) {
                    low[pos] = min(low[pos], dfn[v])
                }
                edge = edge.nextArc
            }

            if (dfn[pos] == low[pos]) {
                cnt++
                do {
                    v = stack.pop()
                    inStack[v] = false
                    visited[v] = cnt
                } while (pos != v)
            }
        }
        for (i in 0..<nodeNum) {
            if (dfn[i] == -1)
                tarjan(i)
        }

        val res = ArrayList<ArrayList<Int>>(cnt)
        for (i in 1..cnt) {
            val scc = ArrayList<Int>()
            for (j in 0..<nodeNum) {
                if (visited[j] == i) {
                    scc.add(j)
                }
            }
            res.add(scc)
        }
        return res
    }

    private fun generateId(): String {
        val builder = StringBuilder()
        nodesArray.forEach {
            var arc = it.firstArc
            builder.append(it.id)
            while (arc != null) {
                builder.append(arc.info.hashCode())
                arc = arc.nextArc
            }
        }
        depthFirstTraversal().forEach {
            builder.append(it.hashCode())
        }
        return encodeBySHA1(builder.toString())
    }

    private fun locatingNode(data: T): Int {
        return nodesArray.indexOf(nodesArray.find { it.data == data })
    }
}