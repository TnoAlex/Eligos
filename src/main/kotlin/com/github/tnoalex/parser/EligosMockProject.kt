package com.github.tnoalex.parser

import org.jetbrains.kotlin.com.intellij.mock.MockProject
import org.jetbrains.kotlin.com.intellij.openapi.Disposable
import org.jetbrains.kotlin.com.intellij.openapi.vfs.VirtualFile
import org.jetbrains.kotlin.org.picocontainer.PicoContainer

class EligosMockProject(parent: PicoContainer?, parentDisposable: Disposable) : MockProject(parent, parentDisposable){
    override fun isDefault(): Boolean {
        return true
    }

    override fun isOpen(): Boolean {
        return true
    }
}