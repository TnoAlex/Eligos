package com.github.tnoalex.foundation.environment

import org.jetbrains.kotlin.com.intellij.mock.MockProject
import org.jetbrains.kotlin.com.intellij.openapi.Disposable
import org.jetbrains.kotlin.com.intellij.openapi.vfs.VirtualFile
import org.jetbrains.kotlin.com.intellij.openapi.vfs.local.CoreLocalFileSystem
import org.jetbrains.kotlin.com.intellij.openapi.vfs.local.CoreLocalVirtualFile
import java.io.File

class JvmMockProject(disposable: Disposable) : MockProject(null, disposable) {
    private lateinit var baseDir: VirtualFile

    @Deprecated("Deprecated in Java")
    override fun getBaseDir(): VirtualFile {
        return baseDir
    }
    fun setBaseDir(dir: VirtualFile) {
        baseDir = dir
    }

    override fun getProjectFile(): VirtualFile {
        return baseDir
    }

    override fun isDefault(): Boolean {
        return true
    }

    override fun isOpen(): Boolean {
        return true
    }

    override fun getBasePath(): String {
        return baseDir.path
    }

    override fun getProjectFilePath(): String {
        return baseDir.path
    }
}