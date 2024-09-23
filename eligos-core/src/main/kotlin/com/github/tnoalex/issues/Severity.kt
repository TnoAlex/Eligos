package com.github.tnoalex.issues

enum class Severity(val level: Int, val describe: String) {
    SUGGESTION(0, "suggestion"),
    CODE_SMELL(1, "code smell"),
    POSSIBLE_BUG(2, "possible bug");
}