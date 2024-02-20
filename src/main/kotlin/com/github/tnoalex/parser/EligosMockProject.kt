package com.github.tnoalex.parser

import com.intellij.mock.MockProject
import com.intellij.openapi.Disposable
import com.intellij.openapi.vfs.VirtualFile
import org.picocontainer.PicoContainer

class EligosMockProject(parent: PicoContainer?, parentDisposable: Disposable) : MockProject(parent, parentDisposable){
    override fun isDefault(): Boolean {
        return true
    }

    override fun isOpen(): Boolean {
        return true
    }
}