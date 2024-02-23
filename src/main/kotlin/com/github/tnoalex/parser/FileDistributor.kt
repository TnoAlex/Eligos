package com.github.tnoalex.parser

interface FileDistributor {
    fun dispatch()
    fun virtualFileConvert(virtualFile: Any): Any
}