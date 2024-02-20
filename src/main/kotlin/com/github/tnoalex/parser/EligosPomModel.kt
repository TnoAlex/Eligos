package com.github.tnoalex.parser

import org.jetbrains.kotlin.com.intellij.openapi.util.UserDataHolderBase
import org.jetbrains.kotlin.com.intellij.pom.PomModel
import org.jetbrains.kotlin.com.intellij.pom.PomModelAspect
import org.jetbrains.kotlin.com.intellij.pom.PomTransaction
import org.jetbrains.kotlin.com.intellij.pom.impl.PomTransactionBase
import org.jetbrains.kotlin.com.intellij.pom.tree.TreeAspect
import sun.reflect.ReflectionFactory

object EligosPomModel: UserDataHolderBase(), PomModel {
    override fun runTransaction(transaction: PomTransaction) {
        val transactionCandidate = transaction as? PomTransactionBase

        val pomTransaction = requireNotNull(transactionCandidate) {
            "${PomTransactionBase::class.simpleName} type expected, actual is ${transaction.javaClass.simpleName}"
        }

        pomTransaction.run()
    }

    override fun <T : PomModelAspect?> getModelAspect(aspect: Class<T>): T? {
        if (aspect == TreeAspect::class.java) {
            val constructor = ReflectionFactory.getReflectionFactory()
                .newConstructorForSerialization(aspect, Any::class.java.getDeclaredConstructor())
            @Suppress("UNCHECKED_CAST")
            return constructor.newInstance() as T
        }
        return null
    }
}