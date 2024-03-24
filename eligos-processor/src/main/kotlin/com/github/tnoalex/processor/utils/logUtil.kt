package com.github.tnoalex.processor.utils

import com.github.tnoalex.processor.EligosProcessorBundle
import com.intellij.psi.PsiElement
import org.slf4j.Logger


internal fun Logger.refCanNotResolveWarn(psiElement: PsiElement) {
    val message = EligosProcessorBundle.message(
        "processor.log.ReferenceCanNotResolveMessage",
        psiElement.filePath,
        psiElement.startLine
    )
    warn(message)
}

internal fun Logger.nameCanNotResolveWarn(type: String, psiElement: PsiElement) {
    val message = EligosProcessorBundle.message(
        "processor.log.NameCanNotResolveMessage",
        type,
        psiElement.filePath,
        psiElement.startLine
    )
    warn(message)
}

internal fun Logger.typeCanNotResolveWarn(type: String, psiElement: PsiElement) {
    val message = EligosProcessorBundle.message(
        "processor.log.TypeCanNotResolveMessage",
        type,
        psiElement.filePath,
        psiElement.startLine
    )
    warn(message)
}

internal fun Logger.kotlinOriginCanNotResolveWarn(type: String, psiElement: PsiElement) {
    val message = EligosProcessorBundle.message(
        "processor.log.KotlinOriginCanNotResolveMessage",
        type,
        psiElement.filePath,
        psiElement.startLine
    )
    warn(message)
}

internal fun Logger.filePathCanNotFindWarn(psiElement: PsiElement) {
    val message = EligosProcessorBundle.message(
        "processor.log.FilePathCanNotFindMessage",
        psiElement.text.replace(System.lineSeparator(), " ")
    )
    warn(message)
}
