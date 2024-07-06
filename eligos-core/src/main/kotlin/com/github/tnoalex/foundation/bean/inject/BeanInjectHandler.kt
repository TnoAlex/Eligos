package com.github.tnoalex.foundation.bean.inject

import com.github.tnoalex.foundation.ApplicationContext
import com.github.tnoalex.foundation.bean.handler.BeansAfterRegisterHandler
import com.github.tnoalex.utils.getMutablePropertiesAnnotateWith
import com.github.tnoalex.utils.getPropertyAnnotation
import com.github.tnoalex.utils.invokePropertySetter
import org.jetbrains.kotlin.utils.addToStdlib.cast
import org.slf4j.LoggerFactory
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.cast
import kotlin.reflect.full.isSubclassOf

@Suppress("unused")
class BeanInjectHandler : BeansAfterRegisterHandler() {
    override val handlerOrder: Int
        get() = 1

    private val currentInjectPoint = ArrayList<KMutableProperty<*>>()

    override fun canHandle(bean: Any): Boolean {
        getMutablePropertiesAnnotateWith(InjectBean::class, bean::class).let { currentInjectPoint.addAll(it) }
        return currentInjectPoint.isNotEmpty()
    }

    override fun dispose() {
        ApplicationContext.removeBeanOfType(this::class.java)
    }

    override fun doHandle(bean: Any) {
        currentInjectPoint.forEach {
            val ann = getPropertyAnnotation<InjectBean>(it)
            val beanName = ann.beanName
            val beanType = ann.beanType
            val propertyType = it.returnType.classifier as? KClass<*> ?: return@forEach
            if (injectPointIsMutableCollection(propertyType)) {
                injectCollection(beanName, beanType, bean, propertyType, it)
            } else {
                if (beanName.isNotBlank()) {
                    ApplicationContext.getBean(beanName)?.let { b ->
                        invokePropertySetter(bean, it, arrayOf(propertyType.cast(b)))
                        return@forEach
                    }
                }
                val beans = if (beanType != Any::class) {
                    val t = ApplicationContext.getBeanOfType(beanType.java)
                    if (t.isEmpty()) ApplicationContext.getBeanOfType(propertyType.java) else t
                } else {
                    ApplicationContext.getBeanOfType(propertyType.java)
                }
                if (beans.size > 1) {
                    logger.error("There are multiple candidate beans, and the exact type cannot be determined")
                    return@forEach
                }
                if (beans.isEmpty()){
                    logger.error(
                        "Can not find bean with bean name $beanName," +
                                "bean type ${if (beanType == Any::class) propertyType.qualifiedName else beanType.qualifiedName} " +
                                "in Application Context"
                    )
                    return@forEach
                }
                invokePropertySetter(bean,it, arrayOf(propertyType.cast(beans.first())))
            }
        }
        currentInjectPoint.clear()
    }

    @Suppress("UNCHECKED_CAST")
    private fun injectCollection(
        beanName: String,
        beanType: KClass<*>,
        bean: Any,
        propertyType: KClass<*>,
        it: KMutableProperty<*>
    ) {
        if (beanName.isBlank() && beanType == Any::class) {
            logger.error("Can not inject bean into a collection with unknown type in ${bean::class.qualifiedName}")
        }
        try {
            val set = propertyType.java.getConstructor().newInstance() as MutableCollection<Any>
            if (beanName.isNotBlank()) {
                ApplicationContext.getBean(beanName)?.let { it1 -> set.add(it1) }
            }
            if (beanType != Any::class) {
                set.addAll(ApplicationContext.getBeanOfType(beanType.java))
            }
            invokePropertySetter(bean, it, arrayOf(set))
        } catch (e: Exception) {
            logger.error("Can not create a collection of type ${beanType.qualifiedName} with it's parameterless constructor")
        }
    }

    private fun injectPointIsMutableCollection(injectPointType: KClass<*>): Boolean {
        return injectPointType.isSubclassOf(MutableCollection::class)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(BeanInjectHandler::class.java)
    }
}