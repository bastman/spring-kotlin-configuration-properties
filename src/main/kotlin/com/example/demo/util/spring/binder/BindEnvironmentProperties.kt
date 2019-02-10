package com.example.demo.util.spring.binder

import com.example.demo.config.Jackson
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.convertValue
import io.burt.jmespath.Expression
import io.burt.jmespath.JmesPath
import io.burt.jmespath.jackson.JacksonRuntime
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
        error("Failed to decode Environment.$name as ${T::class.qualifiedName} (isNullable=$isNullable)! reason: ${all.message}")
    }
}

inline fun <reified T> Environment.jmespath(query: String, noinline om: () -> ObjectMapper = { Jackson.defaultMapper() }): T {
    val JSON: ObjectMapper = om().copy()
    return jq(query = query, json = om) {
        JSON.convertValue(it)
    }
}

inline fun <reified T> Environment.jq(
        query: String,
        noinline json: () -> ObjectMapper = { Jackson.defaultMapper() },
        noinline convert: (Any) -> T
): T {
    val isNullable: Boolean = null is T
    try {
        val expression: Expression<JsonNode> = JmesPathJackson.compile(query)
        val parts: List<String> = query.split(".")
        val rootNodeName: String = parts.firstOrNull()
                ?: error("jmespath query.0 is not set!")
        val rootNodeValue: JsonNode? = decode(rootNodeName) { json().convertValue(it) }
        val data: Map<String, JsonNode?> = mapOf(
                rootNodeName to rootNodeValue
        )
        val dataNode: JsonNode = json().convertValue(data)
        val resultNode: JsonNode? = expression.search(dataNode)

        return when (resultNode) {
            null -> null
            else -> convert(resultNode)
        } as T
    } catch (all: Exception) {
        error("Failed to jq Environment.$query as ${T::class.qualifiedName}  (isNullable=$isNullable)! reason: ${all.message}")
    }

}

object JmesPathJackson {
    private val JACKSON_RUNTIME = JacksonRuntime()

    private val runtime: JmesPath<JsonNode> = JACKSON_RUNTIME
    fun compile(expression: String): Expression<JsonNode> = runtime.compile(expression)
    fun search(input: JsonNode, expression: Expression<JsonNode>): JsonNode = expression.search(input)
}

