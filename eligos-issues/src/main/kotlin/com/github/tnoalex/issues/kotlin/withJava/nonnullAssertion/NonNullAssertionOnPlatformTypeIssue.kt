package com.github.tnoalex.issues.kotlin.withJava.nonnullAssertion

import com.github.tnoalex.issues.*

class NonNullAssertionOnPlatformTypeIssue(
    affectedFile: String,
    content: String,
    startLine: Int,
) : NonNullAssertionIssue(
    EligosIssueBundle.message("issue.name.NonNullAssertionOnPlatformTypeIssue"),
    affectedFile,
    content,
    normal,
    startLine
) {
    companion object {
        @JvmStatic
        val normal: ConfidenceLevel = ConfidenceLevel.LOW
    }
}