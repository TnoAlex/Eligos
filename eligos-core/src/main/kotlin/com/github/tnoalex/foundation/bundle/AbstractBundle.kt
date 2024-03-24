package com.github.tnoalex.foundation.bundle

import com.github.tnoalex.foundation.bundle.util.DefaultBundleService
import com.github.tnoalex.utils.scanEntries
import org.reflections.scanners.Scanners
import org.slf4j.LoggerFactory
import java.io.InputStream
import java.lang.ref.Reference
import java.lang.ref.SoftReference
import java.util.*
import java.util.function.Supplier

abstract class AbstractBundle {
    private var myBundle: Reference<ResourceBundle>? = null
    private var myDefaultBundle: Reference<ResourceBundle>? = null

    protected val bundleClassLoader: ClassLoader


    protected val myPathToBundle: String

    constructor(bundleClass: Class<*>, pathToBundle: String) {
        myPathToBundle = pathToBundle
        bundleClassLoader = bundleClass.classLoader
    }

    protected constructor(pathToBundle: String) {
        myPathToBundle = pathToBundle
        bundleClassLoader = javaClass.classLoader
    }


    fun getMessage(key: String, vararg params: Any): String {
        return BundleBase.messageOrDefault(resourceBundle, key, null, *params)
    }

    fun getPartialMessage(key: String, unassignedParams: Int, vararg params: Any): String {
        return BundleBase.partialMessage(resourceBundle, key, unassignedParams, arrayOf(*params))
    }

    fun getLazyMessage(key: String, vararg params: Any): () -> String {
        val actualParams: Array<out Any> =
            if (params.isEmpty()) emptyArray() else params
        return { getMessage(key, *actualParams) }
    }

    fun messageOrNull(key: String, vararg params: Any): String? {
        return messageOrNull(resourceBundle, key, *params)
    }

    fun messageOrDefault(
        key: String,
        defaultValue: String?,
        vararg params: Any
    ): String? {
        return messageOrDefault(resourceBundle, key, defaultValue, *params)
    }

    fun containsKey(key: String): Boolean {
        return resourceBundle.containsKey(key)
    }

    val resourceBundle: ResourceBundle
        get() = getResourceBundle(bundleClassLoader)

    private fun getResourceBundle(classLoader: ClassLoader): ResourceBundle {
        val isDefault: Boolean = DefaultBundleService.isDefaultBundle
        var bundle: ResourceBundle? = (if (isDefault) myDefaultBundle else myBundle)?.get()
        if (bundle == null) {
            bundle = resolveResourceBundle(myPathToBundle, classLoader)
            val ref = SoftReference(bundle)
            if (isDefault) {
                myDefaultBundle = ref
            } else {
                myBundle = ref
            }
        }
        return bundle
    }

    private fun resolveResourceBundle(pathToBundle: String, loader: ClassLoader): ResourceBundle {
        return resolveResourceBundleWithFallback(
            {
                findBundle(
                    pathToBundle,
                    loader,
                    MyResourceControl.INSTANCE
                )
            },
            loader, pathToBundle
        )
    }

    protected open fun findBundle(
        pathToBundle: String,
        loader: ClassLoader,
        control: ResourceBundle.Control
    ): ResourceBundle {
        return ResourceBundle.getBundle(pathToBundle, Locale.getDefault(), loader, control)
    }

    fun clearLocaleCache() {
        if (myBundle != null) {
            myBundle!!.clear()
        }
    }

    private class MyResourceControl : ResourceBundle.Control() {
        override fun getFormats(baseName: String): List<String> {
            return FORMAT_PROPERTIES
        }

        override fun newBundle(
            baseName: String,
            locale: Locale,
            format: String,
            loader: ClassLoader,
            reload: Boolean
        ): PropertyResourceBundle? {
            val bundleName = toBundleName(baseName, locale)
            val resourceName = if (bundleName.contains("://")) null else toResourceName(bundleName, "properties")
            if (resourceName == null) {
                return null
            }
            val resource = scanEntries(Scanners.Resources).getResources(resourceName).first()
            val resourceStream = loader.getResourceAsStream(resource) ?: InputStream.nullInputStream()
            resourceStream.use {
                return PropertyResourceBundle(it)
            }
        }

        companion object {
            val INSTANCE: MyResourceControl = MyResourceControl()
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(AbstractBundle::class.java)
        val control: ResourceBundle.Control
            get() = MyResourceControl.INSTANCE

        fun messageOrDefault(
            bundle: ResourceBundle?,
            key: String,
            defaultValue: String?,
            vararg params: Any
        ): String? {
            return if (bundle == null) {
                defaultValue
            } else if (!bundle.containsKey(key)) {
                BundleBase.postprocessValue(
                    bundle,
                    BundleBase.useDefaultValue(bundle, key, defaultValue),
                    *params
                )
            } else {
                BundleBase.messageOrDefault(bundle, key, defaultValue, *params)
            }
        }

        fun message(bundle: ResourceBundle, key: String, vararg params: Any): String {
            return BundleBase.messageOrDefault(bundle, key, null, *params)
        }

        fun messageOrNull(bundle: ResourceBundle, key: String, vararg params: Any): String? {
            val value = messageOrDefault(bundle, key, key, *params)
            return if (key == value) null else value
        }

        protected fun resolveResourceBundleWithFallback(
            firstTry: Supplier<out ResourceBundle>,
            loader: ClassLoader,
            pathToBundle: String
        ): ResourceBundle {
            try {
                return firstTry.get()
            } catch (e: MissingResourceException) {
                logger.info("Cannot load resource bundle from *.properties file, falling back to slow class loading: $pathToBundle")
                ResourceBundle.clearCache(loader)
                return ResourceBundle.getBundle(pathToBundle, Locale.getDefault(), loader)
            }
        }

        protected fun resolveBundle(loader: ClassLoader, pathToBundle: String): ResourceBundle {
            return ResourceBundle.getBundle(pathToBundle, Locale.getDefault(), loader, MyResourceControl.INSTANCE)
        }
    }
}
