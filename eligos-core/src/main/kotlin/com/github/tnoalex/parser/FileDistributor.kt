package com.github.tnoalex.parser

import com.github.tnoalex.foundation.LanguageSupportInfo
import com.github.tnoalex.foundation.LaunchEnvironment

interface FileDistributor : LanguageSupportInfo {
    val launchEnvironment: LaunchEnvironment
    fun init()
    fun dispatch()
    fun virtualFileConvert(virtualFile: Any): Any
}