package com.github.tnoalex

import com.github.tnoalex.foundation.ApplicationContext
import com.github.tnoalex.foundation.bean.container.DefaultBeanContainerScanner
import com.github.tnoalex.foundation.bean.handler.DefaultBeanHandlerScanner
import com.github.tnoalex.foundation.bean.register.DefaultBeanRegisterDistributor
import java.util.*

fun main(args: Array<String>) {
    showBanner()
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


