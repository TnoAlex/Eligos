package com.github.tnoalex.foundation.language

import com.github.tnoalex.utils.equalsIgnoreCase
import kotlin.reflect.full.createInstance

sealed class Language(protected val myName: String, val fileExtension: String) {

    override fun hashCode(): Int {
        var result = myName.hashCode()
        result = 31 * result + fileExtension.hashCode()
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Language

        if (myName != other.myName) return false
        if (fileExtension != other.fileExtension) return false

        return true
    }

    fun asString(): String {
        return myName
    }

    override fun toString(): String {
        return "Language: $myName"
    }


    companion object {
        private val languages = HashMap<String, Language>()

        @JvmStatic
        fun createFromString(str: String): Language? {
            if (languages.isEmpty()) {
                tryFindLanguages()
            }
            languages.keys.forEach {
                if (it.equalsIgnoreCase(str)) {
                    return languages[it]
                }
            }
            return null
        }

        private fun tryFindLanguages() {
            Language::class.sealedSubclasses.forEach {
                val instance = it.objectInstance ?: it.createInstance()
                languages[instance.myName] = instance
            }
        }
    }

    object AnyLanguage : Language("Any", "")
}