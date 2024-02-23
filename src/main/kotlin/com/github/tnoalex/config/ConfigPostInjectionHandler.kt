package com.github.tnoalex.config

import com.github.tnoalex.foundation.ApplicationContext
import com.github.tnoalex.foundation.bean.handler.BeansAfterRegisterHandler

class ConfigPostInjectionHandler : BeansAfterRegisterHandler() {
    override fun canHandle(bean: Any): Boolean {
        return ConfigParser::class.java.isAssignableFrom(bean::class.java)
    }

    override fun doHandle(bean: Any) {
        ApplicationContext.delayRemove(ConfigParser::class.java)
    }
}