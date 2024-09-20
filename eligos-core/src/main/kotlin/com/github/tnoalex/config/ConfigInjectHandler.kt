package com.github.tnoalex.config

import com.github.tnoalex.foundation.ApplicationContext
import com.github.tnoalex.foundation.bean.handler.BeanPreRegisterHandler
import com.github.tnoalex.utils.getMutablePropertiesAnnotateWith
import com.github.tnoalex.utils.getPropertyAnnotation
import com.github.tnoalex.utils.invokePropertySetter
import kotlin.reflect.KMutableProperty

class ConfigInjectHandler : BeanPreRegisterHandler() {
    private var currentInjectPoint: ArrayList<KMutableProperty<*>> = ArrayList()

    override val handlerOrder: Int
        get() = 1

    override fun canHandle(bean: Any): Boolean {
        getMutablePropertiesAnnotateWith(InjectConfig::class, bean::class).let { currentInjectPoint.addAll(it) }
        return currentInjectPoint.isNotEmpty()
    }

    override fun doHandle(bean: Any) {
        currentInjectPoint.forEach {
            val ann = getPropertyAnnotation<InjectConfig>(it)
            val configKey = ann.configKey
            val configValue = selectConfig(configKey)
            invokePropertySetter(bean, it, arrayOf(configValue))
        }
        currentInjectPoint.clear()
    }

    override fun dispose() {
        ApplicationContext.removeBeanOfType(this::class.java)
    }

    @Suppress("UNCHECKED_CAST")
    private fun selectConfig(configKey: String): Any {
        var rulers = (ApplicationContext.getBean("ConfigParser") as ConfigParser).rules
        val keysIterator = configKey.split(".").iterator()
        var key = ""
        while (keysIterator.hasNext()) {
            key = keysIterator.next()
            if (rulers[key] !is HashMap<*, *> && keysIterator.hasNext())
                throw RuntimeException("Can not resolve config with key '$configKey'")
            if (!keysIterator.hasNext())
                break
            rulers = rulers[key] as HashMap<String, Any?>
        }
        return rulers[key] ?: throw RuntimeException("Config can not be null")
    }
}