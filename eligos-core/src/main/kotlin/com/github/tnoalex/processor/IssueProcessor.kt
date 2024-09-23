package com.github.tnoalex.processor

import com.github.tnoalex.issues.Severity

interface IssueProcessor : PsiProcessor {
    /**
     * Because an issue processor can process several types of issues,
     * the severity here is the lowest one.
     */
    val severity: Severity
}