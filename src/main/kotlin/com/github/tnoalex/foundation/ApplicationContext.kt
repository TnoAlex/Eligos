package com.github.tnoalex.foundation

import com.github.tnoalex.foundation.bean.BeanNameManager
import com.github.tnoalex.foundation.bean.BeanScope
import com.github.tnoalex.foundation.bean.container.BeanContainer
import com.github.tnoalex.foundation.bean.container.SimpleSingletonBeanContainer
import com.github.tnoalex.foundation.bean.handler.BeanHandler
import com.github.tnoalex.foundation.bean.handler.BeanPreRegisterHandler
import com.github.tnoalex.foundation.bean.register.BeanRegisterDistributor
import org.reflections.Reflections
import org.reflections.scanners.Scanners
import org.reflections.util.ConfigurationBuilder
import org.slf4j.LoggerFactory
import kotlin.reflect.full.memberProperties

object ApplicationContext {
    private val beanContainers = HashMap<BeanScope, ArrayList<BeanContainer>>()
    val beanPreRegisterHandler = BeanHandler.DefaultBeanHandler()
    private val beanRegisterDistributors: ArrayList<BeanRegisterDistributor> = ArrayList()

    private val logger = LoggerFactory.getLogger(this::class.java)

    fun init() {
        initBeanContainer()
        initBeanPreRegisterHandler()
        beanRegisterDistributors.forEach {
            it.dispatch()
        }
        beanPreRegisterHandler.removeHandlers()
    }

    private fun initBeanContainer() {
        Reflections(
            ConfigurationBuilder()
                .forPackages("")
                .setClassLoaders(arrayOf(Thread.currentThread().contextClassLoader))
                .setScanners(Scanners.SubTypes)
        ).getSubTypesOf(BeanContainer::class.java)?.forEach {
            val scope = getBeanScope(it) ?: throw RuntimeException("Unknown container scope of ${it.simpleName}")
            val instance = it.kotlin.objectInstance!!
            beanContainers.getOrPut(scope) { arrayListOf() }
                .find { c -> c.containerId == instance.containerId }
                ?.let {
                    throw RuntimeException(
                        "Duplicate container IDs in the same scope! " +
                                "Caused by ${instance::class.qualifiedName} with id ${instance.containerId}"
                    )
                }
            beanContainers[scope]!!.add(instance)
        }
    }

    private fun initBeanPreRegisterHandler() {
        Reflections(
            ConfigurationBuilder()
                .forPackages("")
                .setClassLoaders(arrayOf(Thread.currentThread().contextClassLoader))
                .setScanners(Scanners.SubTypes)
        ).getSubTypesOf(BeanPreRegisterHandler::class.java)?.forEach {
            beanPreRegisterHandler.addHandler(it.getDeclaredConstructor().newInstance())
        }
    }

    fun addBeanRegisterDistributor(distributors: List<BeanRegisterDistributor>) {
        beanRegisterDistributors.addAll(distributors)
    }

    fun visitContainers(visitor: (BeanScope, BeanContainer) -> Unit) {
        beanContainers.forEach { (k, v) ->
            v.forEach {
                visitor(k, it)
            }
        }
    }

    fun addBean(beanName: String, bean: Any, container: BeanContainer) {
        container.addBean(beanName, bean)
    }

    fun removeBean(beanName: String) {
        if (!BeanNameManager.containsBean(beanName)) {
            logger.warn("Can not find bean named $beanName")
        }
        beanContainers.values.forEach {
            it.forEach { c ->
                if (c.removeBean(beanName)) {
                    return
                }
            }
        }
    }

    fun removeBean(beanType: Class<*>) {
        if (!BeanNameManager.containsBean(beanType)) {
            logger.warn("Can not find bean with type ${beanType.typeName}")
        }
        beanContainers.values.forEach {
            it.forEach { c ->
                if (c.removeBean(beanType)) {
                    return
                }
            }
        }
    }

    fun getBean(beanName: String): Any? {
        if (!BeanNameManager.containsBean(beanName)) {
            logger.warn("Can not find bean named $beanName")
            return null
        }
        beanContainers.values.forEach {
            it.forEach { c ->
                val bean = c.getBean(beanName)
                if (bean != null) return bean
            }
        }
        logger.warn("Can not find bean named $beanName")
        return null
    }

    fun getBean(beanType: Class<*>): List<Any> {
        val list = ArrayList<Any>()
        if (!BeanNameManager.containsBean(beanType)) {
            logger.warn("Can not find bean with type ${beanType.typeName}")
            return list
        }
        beanContainers.values.forEach {
            it.forEach { c ->
                val beans = c.getBean(beanType)
                if (!beans.isNullOrEmpty()) {
                    list.addAll(beans)
                }
            }
        }
        logger.warn("Can not find bean with type ${beanType.typeName}")
        return list
    }

    fun containsBean(beanName: String): Boolean {
        return SimpleSingletonBeanContainer.containsBean(beanName)
    }

    fun containsBean(beanType: Class<*>): Boolean {
        return SimpleSingletonBeanContainer.containsBean(beanType)
    }

    private fun getBeanScope(clazz: Class<out BeanContainer>): BeanScope? {
        val scope = clazz.kotlin.memberProperties.find { it.name == "scope" }
            ?: throw RuntimeException("Unknown container scope of ${clazz.simpleName}")
        return scope.call(clazz.kotlin.objectInstance!!) as? BeanScope
    }

}