package com.github.tnoalex.foundation.cfg.kotlin

import com.github.tnoalex.foundation.cfg.ControlFlowBuilder
import com.github.tnoalex.foundation.cfg.instruction.IInstruction
import com.github.tnoalex.foundation.cfg.pcfginfo.FunctionInfo

class KotlinControlFlowBuilder(fileName: String) : ControlFlowBuilder {
    val buildCache = HashMap<String, IInstruction>()
    val functionInfos = HashMap<String, FunctionInfo>()
    val closuresMap = HashMap<String, String>()
    override fun enteredFunction(
        functionName: String,
        className: String?,
        closuresParentName: String?,
        closuresParentParamNum: Int?,
        visibilityModifier: String,
        functionModifier: String?,
        inheritanceModifier: String?,
        paramsNums: Int
    ) {
        val functionInfo = FunctionInfo()
        functionInfo.functionName = functionName
        functionInfo.parentClass = className
        functionInfo.closuresParent = closuresParentName
        functionInfo.parentFileName = functionName
        functionInfo.visibilityModifier = visibilityModifier
        functionInfo.functionModifier = functionModifier
        functionInfo.inheritanceModifier = inheritanceModifier
        functionInfo.paramNums = paramsNums
        functionInfos[functionName + paramsNums] = functionInfo
        buildCache[functionName + paramsNums] = functionInfo.cfgEntry
        closuresParentName?.let {
            closuresMap[functionName + paramsNums] = closuresParentName + closuresParentParamNum
        }
    }

    override fun enteredCommonBlock() {

    }

    override fun enteredConditionBlock() {

    }

    override fun enteredMultiConditionBlock() {

    }

    private fun enteredForBlock() {

    }

    private fun enteredWhileBlock() {

    }

    private fun enteredDoWhileBlock() {

    }

    private fun enteredIfBlock() {

    }
}