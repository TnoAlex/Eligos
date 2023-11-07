package com.github.tnoalex.foundation.cfg.instruction

/**
 * Globally unique and used only for initialization
 */
object EmptyInstruction : IInstruction {
    override val instructionId: Long
        get() = -1
}