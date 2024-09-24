package com.github.tnoalex.issues.kotlin.withJava.nonnullAssertion

import com.github.tnoalex.issues.ConfidenceLevel
import com.github.tnoalex.issues.EligosIssueBundle

class NonNullAssertionOnNullableTypeIssue(
    affectedFile: String,
    content: String,
    startLine: Int,
) : NonNullAssertionIssue(
    EligosIssueBundle.message("issue.name.NonNullAssertionOnNullableTypeIssue"),
    affectedFile,
    content,
    normal,
    startLine
) {
    companion object {
        @JvmStatic
        val normal: ConfidenceLevel = ConfidenceLevel.EXTREMELY_LOW
    }
}