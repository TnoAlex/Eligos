package com.github.tnoalex.foundation.cfg.instruction

import com.github.tnoalex.utils.encodeBySHA1ToLong

/**
 * It is used to describe multi-conditional statements, such as switch, when, etc
 */
class MultiConditionInstruction : IInstruction {
    val nextBlocks: ArrayList<IInstruction> = ArrayList()

    override val instructionId: Long by lazy {
        encodeBySHA1ToLong(this::class.qualifiedName + nextBlocks.hashCode())
    }
}