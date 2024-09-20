package com.github.tnoalex.processor

import com.github.tnoalex.issues.Severity

interface IssueProcessor : PsiProcessor {
    val severity: Severity
}