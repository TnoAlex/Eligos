package com.github.tnoalex.foundation.algorithm

import java.util.*

data class AdjacencyArc<T>(
    val adjVex: Int,
    var nextArc: AdjacencyArc<T>?,
    val info: LinkedList<Any> = LinkedList()
)