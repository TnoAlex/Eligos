package com.github.tnoalex.parser

import com.github.tnoalex.foundation.LanguageSupportInfo

interface FileDistributor : LanguageSupportInfo {
    fun init()
    fun dispatch()
    fun virtualFileConvert(virtualFile: Any): Any
}