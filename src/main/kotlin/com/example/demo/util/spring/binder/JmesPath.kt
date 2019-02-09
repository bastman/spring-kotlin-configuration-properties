package com.example.demo.util.spring.binder

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.convertValue
import io.burt.jmespath.Expression
import io.burt.jmespath.JmesPath
import io.burt.jmespath.jackson.JacksonRuntime

class JsonJmesPath(
        val json: ObjectMapper,
        private val runtime: JmesPath<JsonNode> = JACKSON_RUNTIME
) {
    private val jqJson: ObjectMapper = json.copy()
    val converter: ObjectMapper = json.copy()

    companion object {
        private val JACKSON_RUNTIME = JacksonRuntime()
    }

    fun compile(query: String): Expression<JsonNode> = runtime.compile(query)

    fun search(content: String, expression: Expression<JsonNode>): JsonNode = expression.search(
            jqJson.readTree(content)
    )

    fun search(content: String, query: String): JsonNode {
        val expression: Expression<JsonNode> = compile(query = query)

        return search(content = content, expression = expression)
    }

    inline fun <reified T> query(content: String, query: String): T {
        try {
            val expression: Expression<JsonNode> = compile(query = query)
            val resultNode: JsonNode = search(content = content, expression = expression)

            val converted: T = converter.convertValue(resultNode)
            val isNullable: Boolean = null is T
            if (converted == null && !isNullable) {
                error("Failed to convert to (non-nullable) instance of ${T::class} from $resultNode .")
            }
            return converted
        } catch (all: Throwable) {
            error("JQ Failed! query: $query - reason: ${all.message} ")
        }
    }
}
