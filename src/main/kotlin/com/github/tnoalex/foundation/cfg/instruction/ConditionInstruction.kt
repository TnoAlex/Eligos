package com.github.tnoalex.foundation.cfg.instruction

import com.github.tnoalex.utils.encodeBySHA1ToLong

/**
 * It is used to describe blocks such as if,while,for,try etc.
 * that can be considered conditional statements at the assembly level
 */
class ConditionInstruction : IInstruction {
    var trueBlock: IInstruction = EmptyInstruction
    var falseBlock: IInstruction = EmptyInstruction

    override val instructionId: Long by lazy {
        encodeBySHA1ToLong(this::class.qualifiedName + trueBlock.hashCode() + falseBlock.hashCode())
    }
}