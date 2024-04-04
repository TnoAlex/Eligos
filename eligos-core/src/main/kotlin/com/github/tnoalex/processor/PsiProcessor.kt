package com.github.tnoalex.processor

import com.github.tnoalex.issues.Severity

interface PsiProcessor : BaseProcessor {
    val severity: Severity
}