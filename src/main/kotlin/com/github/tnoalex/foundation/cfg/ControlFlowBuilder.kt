package com.github.tnoalex.foundation.cfg

interface ControlFlowBuilder {
    fun enteredFunction(
        functionName: String,
        className: String?,
        closuresParentName: String?,
        closuresParentParamNum: Int?,
        visibilityModifier: String,
        functionModifier: String?,
        inheritanceModifier: String?,
        paramsNums: Int
    )

    fun enteredCommonBlock()

    fun enteredConditionBlock()

    fun enteredMultiConditionBlock()
}