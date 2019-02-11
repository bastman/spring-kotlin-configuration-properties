package com.example.demo.util.spring.binder

import org.springframework.boot.context.properties.bind.Bindable
import org.springframework.boot.context.properties.bind.Binder
import org.springframework.core.env.Environment

fun Environment.bindAnyOrNull(name: String): Any? {
    val binder: Binder = Binder.get(this)
    val src = listOf(
            { binder.bind(name, Any::class.java) },
            { binder.bind(name, Bindable.listOf(Any::class.java)) },
            { binder.bind(name, Bindable.mapOf(Any::class.java, Any::class.java)) }
    )
    src.forEach { fn ->
        val r = fn()
        if (r.isBound) {
            return r.get()
        }
    }
    return null
}

inline fun <reified T> Environment.decode(name: String, noinline convert: (Any) -> T): T {
    val isNullable: Boolean = null is T
    try {
        val rawValue: Any? = bindAnyOrNull(name = name)
        return when (rawValue) {
            null -> null
            else -> convert(rawValue)
        } as T
    } catch (all: Exception) {
        val className: String = buildString {
            append(T::class.qualifiedName)
            if (isNullable) {
                append("?")
            }
        }
        error("Failed to decode Environment.$name as $className ! reason: ${all.message}")
    }
}

