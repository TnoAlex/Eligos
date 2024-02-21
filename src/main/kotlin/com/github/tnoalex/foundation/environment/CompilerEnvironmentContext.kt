package com.github.tnoalex.foundation.environment

import java.io.File

interface CompilerEnvironmentContext {
    /*
    * Due to the special package structure of the kotlin compiler, it is not possible to give a specific parameter interface type
    * */
    fun registerCoreFileType(fileType: Any, extension: String)
    fun registerParserDefinition(definition: Any, disposable: Any)

    fun initPsiApplication()
    fun initPsiProject()
    fun createVirtualFile(file: File): Any
}