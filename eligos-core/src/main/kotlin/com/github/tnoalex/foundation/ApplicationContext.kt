package com.github.tnoalex.foundation

import com.github.tnoalex.foundation.bean.BeanNameManager
import com.github.tnoalex.foundation.bean.BeanScope
import com.github.tnoalex.foundation.bean.Suitable
import com.github.tnoalex.foundation.bean.container.BeanContainer
import com.github.tnoalex.foundation.bean.container.BeanContainerScanner
import com.github.tnoalex.foundation.bean.container.SimpleSingletonBeanContainer
import com.github.tnoalex.foundation.bean.handler.*
import com.github.tnoalex.foundation.bean.register.BeanRegisterDistributor
import org.slf4j.LoggerFactory
import java.lang.reflect.Modifier
import java.util.Comparator
import java.util.PriorityQueue
import kotlin.reflect.full.memberProperties

object ApplicationContext {

    var currentClassLoader: ClassLoader = Thread.currentThread().contextClassLoader
    var launchEnvironment: LaunchEnvironment = LaunchEnvironment.CLI
    private val beanPreRemoveHandler = BeanHandler.DefaultBeanHandler()
    private val beanAfterRemoveHandler = BeanHandler.DefaultBeanHandler()
    private val beanPreRegisterHandler = BeanHandler.DefaultBeanHandler()
    private val beanPostRegisterHandler = BeanHandler.DefaultBeanHandler()
    private val beansAfterRegisterHandler = BeanHandler.DefaultBeanHandler()

    var isInitialized = false
        private set
    private val beanContainers = HashMap<BeanScope, ArrayList<BeanContainer>>()
    private val beanRegisterDistributors: ArrayList<BeanRegisterDistributor> = ArrayList()
    private val beanContainerScanners: ArrayList<BeanContainerScanner> = ArrayList()
    private val beanHandlerScanners: ArrayList<BeanHandlerScanner> = ArrayList()
    private val delayRemoveCache = HashSet<Any>()

    private val logger = LoggerFactory.getLogger(this::class.java)

    fun init() {
        if (!isInitialized) {
            initBeanContainer()
            initBeanHandler()
            beanRegisterDistributors.forEach {
                it.dispatch()
            }
            beanContainerScanners.clear()
            beanHandlerScanners.clear()
            beanRegisterDistributors.clear()
            beanPreRegisterHandler.removeHandlers()
            invokeAfterBeansRegisterHandler()
            beansAfterRegisterHandler.removeHandlers()
        }
        isInitialized = true
    }

    fun solveComponentEnv() {
        val unSuitableComponents = ArrayList<String>()
        visitContainers { _, beanContainer ->
            beanContainer.visitBeans { name, bean ->
                val clazz = bean.javaClass
                if (!clazz.isAnnotationPresent(Suitable::class.java)) return@visitBeans
                val suitable = clazz.getAnnotation(Suitable::class.java)
                if (!LaunchEnvironment.isGreaterThan(launchEnvironment, suitable.environment)) {
                    unSuitableComponents.add(name)
                }
            }
        }
        unSuitableComponents.forEach {
            removeBean(it)
        }
    }

    private fun initBeanContainer() {
        val beanContainerClasses = ArrayList<Class<out BeanContainer>>()
        beanContainerScanners.forEach {
            it.scanBeanContainers()?.let { s -> beanContainerClasses.addAll(s) }
        }
        beanContainerClasses.forEach {
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

    private fun createBeanHandlers(): Array<PriorityQueue<BeanHandler>> {
        val beanHandlerInstances =
            Array<PriorityQueue<BeanHandler>>(5) { PriorityQueue { o1, o2 -> o2.handlerOrder - o1.handlerOrder } }
        beanHandlerScanners.mapNotNull { it.scanBeanHandler() }.flatten().forEach {
            if (Modifier.isAbstract(it.modifiers)) return@forEach
            val instance = it.getDeclaredConstructor().newInstance()
            when {
                BeanPreRegisterHandler::class.java.isAssignableFrom(it) -> {
                    beanHandlerInstances[0].offer(instance)
                }

                BeanPostRegisterHandler::class.java.isAssignableFrom(it) -> {
                    beanHandlerInstances[1].offer(instance)
                }

                BeansAfterRegisterHandler::class.java.isAssignableFrom(it) -> {
                    beanHandlerInstances[2].offer(instance)
                }

                BeanPreRemoveHandler::class.java.isAssignableFrom(it) -> {
                    beanHandlerInstances[3].offer(instance)
                }

                BeanAfterRemoveHandler::class.java.isAssignableFrom(it) -> {
                    beanHandlerInstances[4].offer(instance)
                }
            }
        }
        return beanHandlerInstances
    }

    private fun addBeanHandler(handler: BeanHandler, newHandlerQueue: PriorityQueue<BeanHandler>) {
        while (newHandlerQueue.isNotEmpty()) {
            handler.addHandler(newHandlerQueue.poll())
        }
    }

    private fun initBeanHandler() {
        createBeanHandlers().forEachIndexed { index, item ->
            when (index) {
                0 -> {
                    addBeanHandler(beanPreRegisterHandler, item)
                }

                1 -> {
                    addBeanHandler(beanPostRegisterHandler, item)
                }

                2 -> {
                    addBeanHandler(beansAfterRegisterHandler, item)
                }

                3 -> {
                    addBeanHandler(beanPreRemoveHandler, item)
                }

                4 -> {
                    addBeanHandler(beanAfterRemoveHandler, item)
                }
            }
        }
    }

    fun addBeanRegisterDistributor(distributors: List<BeanRegisterDistributor>) {
        beanRegisterDistributors.addAll(distributors)
    }

    fun addBeanContainerScanner(containerScanner: List<BeanContainerScanner>) {
        beanContainerScanners.addAll(containerScanner)
    }

    fun addBeanHandlerScanner(handlerScanner: List<BeanHandlerScanner>) {
        beanHandlerScanners.addAll(handlerScanner)
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
                val removedBean = c.removeBean(beanName)
                if (removedBean != null) {
                    if (delayRemoveCache.isNotEmpty() && delayRemoveCache.contains(removedBean)) {
                        delayRemoveCache.remove(removedBean)
                    }
                    return
                }
            }
        }
    }

    fun removeBeanOfType(beanType: Class<*>) {
        if (!BeanNameManager.containsBean(beanType)) {
            logger.warn("Can not find bean with type ${beanType.typeName}")
        }
        beanContainers.values.forEach {
            it.forEach { c ->
                val removedBeans = c.removeBeanOfType(beanType)
                if (removedBeans.isNotEmpty() && delayRemoveCache.isNotEmpty()) {
                    removedBeans.forEach { removedBean ->
                        if (delayRemoveCache.contains(removedBean)) {
                            delayRemoveCache.remove(removedBean)
                        }
                    }
                }
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
                is Class<*> -> removeBeanOfType(it)
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
                if (bean != null && bean !in delayRemoveCache) return bean
            }
        }
        return null
    }

    fun <T> getBeanOfType(beanType: Class<T>): ArrayList<T> {
        val list = ArrayList<T>()
        if (!BeanNameManager.containsBean(beanType)) {
            logger.warn("Can not find bean with type ${beanType.typeName}")
            return list
        }
        beanContainers.values.forEach {
            it.forEach { c ->
                val beans = c.getBeanOfType(beanType)
                if (!beans.isNullOrEmpty()) {
                    list.addAll(beans.filter { b -> !delayRemoveCache.contains(b as Any) })
                }
            }
        }
        return list
    }

    fun <T> getExactBean(beanType: Class<T>): T? {
        if (!BeanNameManager.containsBean(beanType)) {
            logger.warn("Can not find bean with type ${beanType.typeName}")
            return null
        }
        beanContainers.values.forEach {
            it.forEach { c ->
                val bean = c.getExactBean(beanType)
                if (bean != null) {
                    return bean
                }
            }
        }
        return null
    }

    fun invokeBeanPreRemoveHandler(bean: Any) {
        beanPreRemoveHandler.handle(bean)
    }

    fun invokeBeanAfterRemoveHandler(bean: Any) {
        beanAfterRemoveHandler.handle(bean)
    }

    fun invokeBeanPreRegisterHandler(bean: Any) {
        beanPreRegisterHandler.handle(bean)
    }

    fun invokeAfterBeansRegisterHandler() {
        visitContainers { _, beanContainer ->
            beanContainer.visitBeans { _, bean ->
                if (bean !in delayRemoveCache) {
                    beansAfterRegisterHandler.handle(bean)
                }
            }
        }
        doDelayRemove()
    }

    fun invokeBeanPostRegisterHandler(bean: Any) {
        beanPostRegisterHandler.handle(bean)
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