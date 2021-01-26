package io.izzel.taboolib.kotlin.ketherx.util

import org.bukkit.Bukkit
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

/**
 * @Author IzzelAliz
 */
object Closables {

    fun bukkitTask(taskId: Int): AutoCloseable {
        return AutoCloseable { Bukkit.getScheduler().cancelTask(taskId) }
    }

    fun <T : Event> listening(clazz: Class<T>, consumer: (T) -> Unit): AutoCloseable {
        val listener = OnetimeListener.set(clazz, { it.javaClass == clazz }, consumer)
        return AutoCloseable { HandlerList.unregisterAll(listener) }
    }

    fun <T : Event> listening(clazz: Class<T>, predicate: (T) -> Boolean, consumer: (T) -> Unit): AutoCloseable {
        val listener = OnetimeListener.set(clazz, predicate, consumer)
        return AutoCloseable { HandlerList.unregisterAll(listener) }
    }
}