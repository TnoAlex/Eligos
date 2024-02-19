package com.github.tnoalex.foundation

import com.github.tnoalex.foundation.bean.BeanNameManager
import com.github.tnoalex.foundation.bean.BeanScope
import com.github.tnoalex.foundation.bean.container.BeanContainer
import com.github.tnoalex.foundation.bean.container.SimpleSingletonBeanContainer
import com.github.tnoalex.foundation.bean.handler.*
import com.github.tnoalex.foundation.bean.register.BeanRegisterDistributor
import org.reflections.Reflections
import org.reflections.scanners.Scanners
import org.reflections.util.ConfigurationBuilder
import org.slf4j.LoggerFactory
import java.lang.reflect.Modifier
import kotlin.reflect.full.memberProperties

object ApplicationContext {
    private val beanContainers = HashMap<BeanScope, ArrayList<BeanContainer>>()

    val beanPreRegisterHandler = BeanHandler.DefaultBeanHandler()
    val beanPostRegisterHandler = BeanHandler.DefaultBeanHandler()
    private val beansAfterRegisterHandler = BeanHandler.DefaultBeanHandler()
    val beanPreRemoveHandler = BeanHandler.DefaultBeanHandler()
    val beanAfterRemoveHandler = BeanHandler.DefaultBeanHandler()

    private val beanRegisterDistributors: ArrayList<BeanRegisterDistributor> = ArrayList()
    private val delayRemoveCache = ArrayList<Any>()

    private val logger = LoggerFactory.getLogger(this::class.java)

    fun init() {
        initBeanContainer()
        initBeanHandler()
        beanRegisterDistributors.forEach {
            it.dispatch()
        }
        beanPreRegisterHandler.removeHandlers()
        invokeAfterBeansRegisterHandler()
        beansAfterRegisterHandler.removeHandlers()
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

    private fun initBeanHandler() {
        Reflections(
            ConfigurationBuilder()
                .forPackages("")
                .setClassLoaders(arrayOf(Thread.currentThread().contextClassLoader))
                .setScanners(Scanners.SubTypes)
        ).getSubTypesOf(BeanHandler::class.java)?.forEach {
            if (Modifier.isAbstract(it.modifiers)) return@forEach
            when {
                BeanPreRegisterHandler::class.java.isAssignableFrom(it) -> {
                    beanPreRegisterHandler.addHandler(it.getDeclaredConstructor().newInstance())
                }

                BeanPostRegisterHandler::class.java.isAssignableFrom(it) -> {
                    beanPostRegisterHandler.addHandler(it.getDeclaredConstructor().newInstance())
                }

                BeansAfterRegisterHandler::class.java.isAssignableFrom(it) -> {
                    beansAfterRegisterHandler.addHandler(it.getDeclaredConstructor().newInstance())
                }

                BeanPreRemoveHandler::class.java.isAssignableFrom(it) -> {
                    beanPreRemoveHandler.addHandler(it.getDeclaredConstructor().newInstance())
                }

                BeanAfterRemoveHandler::class.java.isAssignableFrom(it) -> {
                    beanAfterRemoveHandler.addHandler(it.getDeclaredConstructor().newInstance())
                }
            }
        }
    }

    private fun invokeAfterBeansRegisterHandler() {
        visitContainers { _, beanContainer ->
            beanContainer.visitBeans { _, bean -> beansAfterRegisterHandler.handle(bean) }
        }
        doDelayRemove()
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
                if (c.removeBean(beanName) != null) {
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
                c.removeBean(beanType)
            }
        }
    }

    fun delayRemove(beanNameOrType: Any) {
        delayRemoveCache.add(beanNameOrType)
    }

    fun doDelayRemove() {
        delayRemoveCache.forEach {
            when (it) {
                is String -> removeBean(it)
                is Class<*> -> removeBean(it)
                else -> throw RuntimeException("Unknown bean type or name")
            }
        }
        delayRemoveCache.clear()
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

    fun <T> getBean(beanType: Class<T>): List<T> {
        val list = ArrayList<T>()
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