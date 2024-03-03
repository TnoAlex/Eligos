package com.github.tnoalex.foundation

enum class LaunchEnvironment(val index: Int) {
    CLI(0),
    IDE_PLUGIN(1);

    companion object {
        fun isGreaterThan(e1: LaunchEnvironment, e2: LaunchEnvironment): Boolean {
            return e1.index - e2.index >= 0
        }
    }
}