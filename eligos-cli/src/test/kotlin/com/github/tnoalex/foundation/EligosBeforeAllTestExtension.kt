package com.github.tnoalex.foundation

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.LoggerContext
import com.github.tnoalex.config.ConfigInjectHandler
import com.github.tnoalex.config.ConfigParser
import com.github.tnoalex.foundation.bean.BeanScope
import com.github.tnoalex.foundation.bean.container.SimpleSingletonBeanContainer
import com.github.tnoalex.foundation.bean.inject.BeanInjectHandler
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class EligosBeforeAllTestExtension : BeforeAllCallback, AfterAllCallback {
    override fun beforeAll(context: ExtensionContext) {
        (LoggerFactory.getILoggerFactory() as LoggerContext).getLogger(Logger.ROOT_LOGGER_NAME).level = Level.OFF
        val configParser = ConfigParser()
        TestApplicationContextProxy.addBeanContainer(BeanScope.Singleton, SimpleSingletonBeanContainer)
        ApplicationContext.addBean(configParser.javaClass.simpleName, configParser, SimpleSingletonBeanContainer)
        TestApplicationContextProxy.getBeanPreRegisterHandler().addHandler(ConfigInjectHandler())
        TestApplicationContextProxy.getBeansAfterRegisterHandler().addHandler(BeanInjectHandler())
    }

    override fun afterAll(context: ExtensionContext) {
        ApplicationContext.removeBeanOfType(ConfigParser::class.java)
    }
}