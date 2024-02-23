package com.github.tnoalex.parser

interface FileDistributor {
    fun init()
    fun dispatch()
    fun virtualFileConvert(virtualFile: Any): Any
}