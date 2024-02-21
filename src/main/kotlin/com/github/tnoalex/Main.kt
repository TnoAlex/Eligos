package com.github.tnoalex

import com.github.tnoalex.foundation.ApplicationContext
import com.github.tnoalex.foundation.bean.register.DefaultBeanRegisterDistributor
import depends.LangRegister
import java.util.*

fun main(args: Array<String>) {
    initApplication()
    showBanner()
    LangRegister.register()
    CommandParser().main(args)
}

private fun showBanner() {
    val inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("banner.txt")
    val banner = StringBuilder()
    if (inputStream != null) {
        val scanner = Scanner(inputStream)
        while (scanner.hasNextLine()) {
            banner.append(scanner.nextLine()).append("\n")
        }
        println(banner.toString())
    }
}

private fun initApplication() {
    ApplicationContext.addBeanRegisterDistributor(listOf(DefaultBeanRegisterDistributor))
    ApplicationContext.init()
}

