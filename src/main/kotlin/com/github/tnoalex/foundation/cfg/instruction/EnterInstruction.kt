package com.github.tnoalex.foundation.cfg.instruction

/**
 * A portal used to describe the method
 */
class EnterInstruction : IInstruction {
    var nextBlock = EmptyInstruction
    override val instructionId: Long
        get() = 0
}