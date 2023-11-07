package com.github.tnoalex.foundation.cfg.instruction

import com.github.tnoalex.utils.encodeBySHA1ToLong

/**
 * A basic code block used to describe a common codebase,
 * usually in addition to if, else, while, for, and other conditions or loop statements
 */
class CommonInstruction : IInstruction {
    var nextBlock: IInstruction = EmptyInstruction
    override val instructionId: Long by lazy {
        encodeBySHA1ToLong(this::class.qualifiedName.toString() + nextBlock.hashCode())
    }
}