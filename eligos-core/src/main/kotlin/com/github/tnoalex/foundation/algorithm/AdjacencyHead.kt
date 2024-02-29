package com.github.tnoalex.foundation.algorithm

data class AdjacencyHead<T>(
    val data: T,
    var firstArc: AdjacencyArc<T>?,
    val id: Int
)