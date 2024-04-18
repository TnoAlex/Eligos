package com.github.tnoalex.foundation

import com.github.tnoalex.config.ConfigInjectHandler
import com.github.tnoalex.config.ConfigParser
import com.github.tnoalex.foundation.bean.BeanScope
import com.github.tnoalex.foundation.bean.container.SimpleSingletonBeanContainer
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtensionContext

class EligosBeforeAllTestExtension : BeforeAllCallback {
    override fun beforeAll(context: ExtensionContext) {
        val configParser = ConfigParser()
        TestApplicationContextProxy.addBeanContainer(BeanScope.Singleton, SimpleSingletonBeanContainer)
        ApplicationContext.addBean(configParser.javaClass.simpleName, configParser, SimpleSingletonBeanContainer)
        TestApplicationContextProxy.getBeanPreRegisterHandler().addHandler(ConfigInjectHandler())
    }
}