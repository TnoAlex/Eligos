package complexmethods


fun ccSample0() {
    for (i in 0..10) {
        println(i)
        if (i % 2 == 0 && (i != 2 || i!=4)) {
            println("$i!")
        } else if (i % 3 == 0 || i % 5 == 0) {
            println("..")
        } else {
            println("...")
        }
    }
}

//fun ccSample1(p1: Int, p2: ArrayList<Int>) { //1
//    val t = listOf(1, 2, 3, 4) as ArrayList<Int> //1
//    when (p1) {
//        10 -> {
//            try {
//                println(p2.last())
//            } catch (e: Exception) {
//                throw RuntimeException()
//            }
//        }
//
//        11 -> {
//            if (p2.size > 10) {
//                while (t.isNotEmpty()) {
//                    if (t[0] > 10) {
//                        t[1]++
//                        continue
//                    } else {
//                        t.removeAt(2)
//                    }
//                }
//            }
//        }
//
//        7 -> {
//            var t = p1
//            loop@ while (t > 1) {
//                while (t < 10) {
//                    t++
//                    break@loop
//                }
//            }
//        }
//    }
//}
//
//fun ccSample2(ctx: KotlinParser.TryExpressionContext) {
//    if (ctx.children.size == 1) return
//    val parentFunc = getParentFunction(ctx)
//            ?: throw RuntimeException("Syntax error, try expression are not allowed to appear at the top level")
//    val id = parentFunc.id()
//    addNode(id, 2)
//    addArc(id, 3)
//    if (ctx.finallyBlock() != null) {
//        addNode(id)
//        addArc(id)
//    }
//}
//
//fun ccSample3(from: T, to: T, info: Any?, strategy: DuplicateEdgeStrategy) {
//    val fromNode = locatingNode(from)
//    val toNode = locatingNode(to)
//
//    fun action(act: (AdjacencyArc<T>) -> Unit) {
//        val newArc = AdjacencyArc(toNode, nodesArray[fromNode].firstArc)
//        val oldArc = newArc.nextArc
//        nodesArray[fromNode].firstArc = newArc
//        oldArc?.nextArc = null
//        arcNum++
//        act(newArc)
//    }
//    if (info == null) {
//        if (isExistArc(fromNode, toNode)) return
//    } else {
//        if (isExistArc(fromNode, toNode)) {
//            when (strategy) {
//                DuplicateEdgeStrategy.APPEND -> {
//                    val arc = getArcBetween(fromNode, toNode)
//                    arc.info.add(info)
//                    return
//                }
//
//                DuplicateEdgeStrategy.DISCARD -> return
//                else -> {
//                    action { it.info.add(info) }
//                }
//            }
//        }
//    }
//    action {}
//}
//
//fun solveSCC(): List<List<Int>> {
//    val dfn = IntArray(nodeNum) { -1 }
//    val low = IntArray(nodeNum) { -1 }
//    val visited = IntArray(nodeNum) { -1 }
//    var index = 0
//    val inStack = BooleanArray(nodeNum) { false }
//    val stack = Stack<Int>()
//    var cnt = 0
//
//    fun tarjan(pos: Int) {
//        var v: Int
//        dfn[pos] = ++index
//        low[pos] = dfn[pos]
//        stack.push(pos)
//        inStack[pos] = true
//
//        var edge = nodesArray[pos].firstArc
//        while (edge != null) {
//            v = edge.adjVex
//            if (dfn[v] == -1) {
//                tarjan(v)
//                low[pos] = min(low[pos], low[v])
//            } else if (inStack[v]) {
//                low[pos] = min(low[pos], dfn[v])
//            }
//            edge = edge.nextArc
//        }
//
//        if (dfn[pos] == low[pos]) {
//            cnt++
//            do {
//                v = stack.pop()
//                inStack[v] = false
//                visited[v] = cnt
//            } while (pos != v)
//        }
//    }
//    for (i in 0..<nodeNum) {
//        if (dfn[i] == -1)
//            tarjan(i)
//    }
//
//    val res = java.util.ArrayList<java.util.ArrayList<Int>>(cnt)
//    for (i in 1..cnt) {
//        val scc = java.util.ArrayList<Int>()
//        for (j in 0..<nodeNum) {
//            if (visited[j] == i) {
//                scc.add(j)
//            }
//        }
//        res.add(scc)
//    }
//    return res
//}
//
//fun tarjan(pos: Int) {
//    var v: Int
//    dfn[pos] = ++index
//    low[pos] = dfn[pos]
//    stack.push(pos)
//    inStack[pos] = true
//
//    var edge = nodesArray[pos].firstArc
//    while (edge != null) {
//        v = edge.adjVex
//        if (dfn[v] == -1) {
//            tarjan(v)
//            low[pos] = min(low[pos], low[v])
//        } else if (inStack[v]) {
//            low[pos] = min(low[pos], dfn[v])
//        }
//        edge = edge.nextArc
//    }
//
//    if (dfn[pos] == low[pos]) {
//        cnt++
//        do {
//            v = stack.pop()
//            inStack[v] = false
//            visited[v] = cnt
//        } while (pos != v)
//    }
//}