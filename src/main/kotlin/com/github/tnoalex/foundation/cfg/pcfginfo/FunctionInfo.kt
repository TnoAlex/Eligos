package com.github.tnoalex.foundation.cfg.pcfginfo

import com.github.tnoalex.foundation.cfg.instruction.EnterInstruction

class FunctionInfo {
    var functionName: String = ""
    var closuresParent: String? = null
    var parentFileName: String = ""
    var parentClass: String? = null
    var paramNums: Int = -1
    var lineNumber: Int = -1
    var visibilityModifier: String = ""
    var functionModifier: String? = null
    var inheritanceModifier: String? = null
    var cfgEntry: EnterInstruction = EnterInstruction()
    val isTopLevelFunc: Boolean
        get() = parentClass == null
}