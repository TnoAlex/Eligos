package com.github.tnoalex.plugin.bean

import com.github.tnoalex.foundation.bean.Component
import com.github.tnoalex.foundation.bean.container.BeanContainer
import com.github.tnoalex.foundation.bean.container.BeanContainerScanner
import com.github.tnoalex.foundation.bean.handler.BeanHandler
import com.github.tnoalex.foundation.bean.handler.BeanHandlerScanner
import com.github.tnoalex.foundation.bean.register.BeanRegister
import com.github.tnoalex.foundation.bean.register.BeanRegisterDistributor
import java.lang.reflect.Modifier
import java.net.JarURLConnection

class IdeBeanSupportStructureScanner(private val classLoader: ClassLoader) :
    BeanRegisterDistributor,
    BeanContainerScanner,
    BeanHandlerScanner {
    private val classCache = ArrayList<Class<*>>()

    @Suppress("UNCHECKED_CAST")
    override fun scanDispatchers(): List<Class<out BeanRegister>> {
        checkCache()
        return classCache.filter {
            BeanRegister::class.java.isAssignableFrom(it) &&
                    !Modifier.isInterface(it.modifiers) &&
                    !Modifier.isAbstract(it.modifiers)
        }.map { it as Class<out BeanRegister> }
    }

    override fun scanComponent(): List<Class<*>> {
        checkCache()
        return classCache.filter { it.isAnnotationPresent(Component::class.java) }
            .filter { !Modifier.isInterface(it.modifiers) && !Modifier.isAbstract(it.modifiers) }
    }

    @Suppress("UNCHECKED_CAST")
    override fun scanBeanContainers(): List<Class<out BeanContainer>> {
        checkCache()
        return classCache.filter {
            BeanContainer::class.java.isAssignableFrom(it) &&
                    !Modifier.isInterface(it.modifiers) &&
                    !Modifier.isAbstract(it.modifiers)
        }.map { it as Class<out BeanContainer> }
    }

    @Suppress("UNCHECKED_CAST")
    override fun scanBeanHandler(): List<Class<out BeanHandler>> {
        checkCache()
        return classCache.filter {
            BeanHandler::class.java.isAssignableFrom(it) &&
                    !Modifier.isInterface(it.modifiers) &&
                    !Modifier.isAbstract(it.modifiers)
        }.map { it as Class<out BeanHandler> }
    }

    private fun checkCache() {
        if (classCache.isEmpty()) {
            loadClass()
        }
    }

    private fun loadClass() {
        val extensionList = classLoader.getResource("eligos.extension.pkgs")?.readText()
            ?: throw RuntimeException("Can not find eligos components or components config is null !")
        extensionList.split("\n").forEach { ext ->
            val resourcePath = ext.replace(".", "/")
            val urls = classLoader.getResources(resourcePath)
            while (urls.hasMoreElements()) {
                val dir = urls.nextElement()
                if (dir.protocol != "jar") continue
                val urlConnection = dir.openConnection() as JarURLConnection
                val entities = urlConnection.jarFile.entries()
                while (entities.hasMoreElements()) {
                    val entity = entities.nextElement()
                    if (!entity.name.endsWith(".class") || entity.name.contains("$")) continue
                    val fPath = entity.name.replace("\\\\", "/")
                    var packageName = fPath.substring(fPath.lastIndexOf(resourcePath))
                    packageName = packageName.replace(".class", "").replace("/", ".")
                    val clazz = classLoader.loadClass(packageName)
                    classCache.add(clazz)
                }
            }
        }
    }
}