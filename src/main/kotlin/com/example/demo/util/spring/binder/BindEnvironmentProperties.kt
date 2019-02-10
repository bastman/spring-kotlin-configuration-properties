package com.example.demo.util.spring.binder

import com.example.demo.config.Jackson
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.convertValue
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
    try {
        val rawValue: Any? = bindAnyOrNull(name = name)
        return when (rawValue) {
            null -> null
            else -> convert(rawValue)
        } as T
    } catch (all: Exception) {
        error("Failed to decode Environment.$name as ${T::class.qualifiedName} ! reason: ${all.message}")
    }
}

inline fun <reified T> Environment.jmespath(query: String, om: () -> ObjectMapper = { Jackson.defaultMapper() }): T =
        try {
            val JSON: ObjectMapper = om()
            val parts: List<String> = query.split(".")
            val first: String = parts.firstOrNull()
                    ?: error("jmespath query.0 is not set!")

            val rootNodeName: String = first
            val rootNodeValue: Map<String, Any?> = decode(rootNodeName) {
                JSON.copy().convertValue(it)
            }

            val context: Map<String, Any?> = mapOf(rootNodeName to rootNodeValue)
            val jp = JsonJmesPath(json = JSON.copy())

            jp.query(
                    content = JSON.writeValueAsString(context),
                    query = query
            )
        } catch (all: Throwable) {
            error("Failed to get properties from environment! query=$query reason=${all.message}")
        }


/*

fun <T:Any>Environment.decode(om:ObjectMapper, name:String, klass: KClass<T>):T {
    val rawValue:Any? = bindAnyOrNull(name=name)
    if(rawValue==null) {
        if(klass.isInstance(rawValue)) {
            return klass.cast(rawValue)
        }
    }
    return om.convertValue(rawValue, klass.java)
}


inline fun <reified T> Environment.decode2(om: ObjectMapper,name:String): T {
    try {
        val rawValue:Any? = bindAnyOrNull(name=name)
        if(rawValue==null) {
            return null as T
        }
        val sink:T = om.convertValue(rawValue)
        return sink as T
    }catch (all:Exception) {
        error("Failed to decode Environment.$name as ${T::class.qualifiedName} ! reason: ${all.message}")
    }
}




    fun <T:Any>Environment.bindProperties(
            name: String, objectMapper: ObjectMapper, klass: KClass<T>
    ):T {
        val r:T= decode(objectMapper, name, klass)
        return r
    }


inline fun <reified T : Any> Environment.bindProperties(
        name: String, objectMapper: ObjectMapper
): T = bindProperties(name,objectMapper, T::class)

*/