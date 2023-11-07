package com.github.tnoalex.foundation.cfg.instruction

/**
 * It is used to describe function exits, such as return, throw, etc.
 */
class ExitInstruction : IInstruction {
    override val instructionId: Long
        get() = Long.MAX_VALUE
}