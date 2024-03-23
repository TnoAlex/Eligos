package com.github.tnoalex.processor.utils

import com.github.tnoalex.foundation.ApplicationContext
import com.github.tnoalex.foundation.PropertyManager
import com.intellij.psi.PsiElement
import org.slf4j.Logger
import java.text.MessageFormat

private var processorMessageCache: HashMap<String, String> = HashMap()

private fun getMessage(messageKey: String): String {
    if (processorMessageCache.isEmpty()) {
        val rawMap =
            ApplicationContext.getExactBean(PropertyManager::class.java)!!.getMajorMap("processor") ?: return ""
        processorMessageCache.putAll(rawMap)
    }
    return processorMessageCache[messageKey] ?: ""
}

internal fun Logger.refCanNotResolveWarn(psiElement: PsiElement) {
    val template = getMessage("ReferenceCanNotResolveMessage")
    val message = MessageFormat.format(template, psiElement.filePath, psiElement.startLine)
    warn(message)
}

internal fun Logger.nameCanNotResolveWarn(type: String, psiElement: PsiElement) {
    val template = getMessage("NameCanNotResolveMessage")
    val message = MessageFormat.format(template, type, psiElement.filePath, psiElement.startLine)
    warn(message)
}

internal fun Logger.typeCanNotResolveWarn(type: String, psiElement: PsiElement) {
    val template = getMessage("TypeCanNotResolveMessage")
    val message = MessageFormat.format(template, type, psiElement.filePath, psiElement.startLine)
    warn(message)
}

internal fun Logger.kotlinOriginCanNotResolveWarn(type: String, psiElement: PsiElement) {
    val template = getMessage("KotlinOriginCanNotResolveMessage")
    val message = MessageFormat.format(template, type, psiElement.filePath, psiElement.startLine)
    warn(message)
}

internal fun Logger.filePathCanNotFindWarn(psiElement: PsiElement) {
    val template = getMessage("FilePathCanNotFindMessage")
    val message = MessageFormat.format(template, psiElement.text.replace(System.lineSeparator(), " "))
    warn(message)
}
