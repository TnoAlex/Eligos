package com.github.tnoalex.statistics

import com.github.tnoalex.formatter.Formatable

interface Statistics : Formatable {
    var fileNumber: Int
    var lineNumber: Int
}