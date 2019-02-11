package com.example.demo.util.spring.binder

import com.example.demo.config.Jackson
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.convertValue
import io.burt.jmespath.Expression
import io.burt.jmespath.JmesPath
import io.burt.jmespath.jackson.JacksonRuntime
import org.springframework.core.env.Environment

inline fun <reified T> Environment.jmespath(query: String, noinline om: () -> ObjectMapper = { Jackson.defaultMapper() }): T {
    val JSON: ObjectMapper = om().copy()
    return jq(query = query, json = om) {
        JSON.convertValue(it)
    }
}

inline fun <reified T> Environment.jq(
        root: String,
        query: String,
        noinline json: () -> ObjectMapper = { Jackson.defaultMapper() },
        noinline convert: (Any) -> T
): T {
    val isNullable: Boolean = null is T
    try {
        val expression: Expression<JsonNode> = JmesPathJackson.compile(query)
        val rootNodeValue: JsonNode? = decode(root) { json().convertValue(it) }
        val theRootNode:JsonNode = if(rootNodeValue==null && isNullable) {
                return null as T
        } else {
            rootNodeValue as JsonNode
        }
        val resultNode: JsonNode? = expression.search(theRootNode)
        return when (resultNode) {
            null -> null
            else -> convert(resultNode)
        } as T
    } catch (all: Exception) {
        val className:String = buildString {
            append(T::class.qualifiedName)
            if(isNullable) { append("?") }
        }
        error("Failed to jq Environment.$root (query=$query) as $className ! reason: ${all.message}")
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
        val className:String = buildString {
            append(T::class.qualifiedName)
            if(isNullable) { append("?") }
        }
        error("Failed to jq Environment.$query as $className ! reason: ${all.message}")
    }

}

object JmesPathJackson {
    private val JACKSON_RUNTIME = JacksonRuntime()

    private val runtime: JmesPath<JsonNode> = JACKSON_RUNTIME
    fun compile(expression: String): Expression<JsonNode> = runtime.compile(expression)
    fun search(input: JsonNode, expression: Expression<JsonNode>): JsonNode = expression.search(input)
}

