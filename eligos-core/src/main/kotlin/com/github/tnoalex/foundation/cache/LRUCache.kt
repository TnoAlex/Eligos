package com.github.tnoalex.foundation.cache

class LRUCache<K, V>(private val maxEntities: Int = 8, loadFactor: Float = 0.75f) :
    LinkedHashMap<K, V>(maxEntities, loadFactor, true) {
    override fun removeEldestEntry(eldest: MutableMap.MutableEntry<K, V>?): Boolean {
        return size > maxEntities
    }
}