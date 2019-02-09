package com.example.demo.util.spring.binder

import com.example.demo.config.Jackson
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.convertValue
import org.springframework.boot.context.properties.bind.Bindable
import org.springframework.boot.context.properties.bind.Binder
import org.springframework.core.env.Environment

/*
Example:
val stuff:MyAwesomeDataClass = bindEnvironmentProperties(
                environment = env,
                name = "example.kotlin",
                objectMapper = Jackson.defaultMapper()
        )

val stuff:Map<String,Any?> = bindEnvironmentProperties(
                environment = env,
                name = "example.kotlin",
                objectMapper = Jackson.defaultMapper()
        )

 val stuff:List<Any?> = bindEnvironmentProperties(
                environment = env,
                name = "example.kotlin",
                objectMapper = Jackson.defaultMapper()
        )
*/



inline fun <reified T : Any> bindEnvironmentProperties(
        environment: Environment, name: String, objectMapper: ObjectMapper
): T {
    val data: Any = run {
        try {
            val asList = Binder.get(environment)
                    .bind(name, Bindable.listOf(Any::class.java)).get()
            return@run asList
        } catch (all: Exception) {
        }
        try {
            val asMap = Binder.get(environment)
                    .bind(name, Bindable.mapOf(String::class.java, Any::class.java)).get()
            return@run asMap
        } catch (all: Exception) {
        }

    } ?: throw RuntimeException(
            "Failed to convert content of environment.$name to ${T::class.java} !"
                    + " Expected: environment.$name to be of type Map<String,Any?> or List<Any?>"
                    + " reason: No value bound"
    )

    try {
        return objectMapper.convertValue(data)
        //val content = objectMapper.writeValueAsString(data)
        //return objectMapper.readValue(content, jacksonTypeRef<T>())
    } catch (all: Exception) {
        throw RuntimeException(
                "Failed to convert content of environment.$name to ${T::class.java} !"
                        + " content: ${objectMapper.writeValueAsString(data)}"
                        + " reason: ${all.message}"
        )
    }

}


inline fun <reified T> Environment.jmespath(query: String, om: () -> ObjectMapper = { Jackson.defaultMapper() }): T =
        try {
            val JSON: ObjectMapper = om()
            val parts = query.split(".")
            val first = parts.firstOrNull()
                    ?: error("jmespath query.0 is not set!")

            val rootNodeName = first
            val rootNodeValue: Map<String, Any?> = bindEnvironmentProperties(
                    environment = this,
                    objectMapper = JSON.copy(),
                    name = rootNodeName
            )
            val context: Map<String, Any?> = mapOf(rootNodeName to rootNodeValue)
            val jp = JsonJmesPath(json = JSON.copy())
            jp.query(
                    content = JSON.writeValueAsString(context),
                    query = query
            )
        } catch (all: Throwable) {
            error("Failed to get properties from environment! query=$query reason=${all.message}")
        }