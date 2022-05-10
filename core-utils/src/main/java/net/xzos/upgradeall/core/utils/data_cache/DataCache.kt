package net.xzos.upgradeall.core.utils.data_cache

import android.os.Build
import net.xzos.upgradeall.core.utils.coroutines.CoroutinesMutableMap
import net.xzos.upgradeall.core.utils.coroutines.coroutinesMutableMapOf
import java.time.Instant


class DataCache(private val defExpires: Int, private val autoRemove: Boolean = true) {

    private val time
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            Instant.now().epochSecond
        else System.currentTimeMillis() / 1000

    private val cache = Cache()

    private fun Pair<Any?, Long>.expired(): Boolean {
        return time > this.second
    }

    fun getAll(): Map<String, Any?> {
        val map = mutableMapOf<String, Any?>()
        cache.anyCacheMap.forEach {
            if (it.value.expired()) {
                if (autoRemove) remove(it.key)
            } else {
                map[it.key] = it.value.first
            }
        }
        return map
    }

    fun <E> get(key: String): E? {
        return getRaw<E>(key)?.let {
            if (it.second) {
                if (autoRemove) remove(key)
                null
            } else it.first
        }
    }


    fun <E> getRaw(key: String): Pair<E?, Boolean>? {
        return cache.anyCacheMap[key]?.let {
            @Suppress("UNCHECKED_CAST")
            Pair(it.first as E, it.expired())
        }
    }

    fun cache(key: String, value: Any?, expiresSec: Int = defExpires) {
        cache.anyCacheMap[key] = Pair(value, time + expiresSec)
    }

    fun remove(key: String) {
        cache.anyCacheMap.remove(key)
    }

    companion object {
        class Cache(
            val anyCacheMap: CoroutinesMutableMap<String,
                    Pair<Any?, Long>> = coroutinesMutableMapOf(true),
        )
    }
}