package com.github.tnoalex.config

class FunctionConfig : AbstractConfig("Function") {
    var arity: Int = 0
        private set
    var maxCyclomaticComplexity: Int = 0
        private set
}