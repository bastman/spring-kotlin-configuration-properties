package com.example.demo.rest

import com.example.demo.config.Jackson
import com.example.demo.config.encode
import com.example.demo.exampleJob.ExampleJobConfig
import com.example.demo.exampleJob.ExampleJobResult
import com.example.demo.exampleJob.ExampleJobService
import com.example.demo.util.spring.binder.decode
import com.example.demo.util.spring.binder.jmespath
import com.example.demo.util.spring.binder.jq
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.convertValue
import mu.KLogging
import org.springframework.core.env.Environment
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.Duration


@RestController
class ApiController(
        private val env: Environment,
        private val exampleJobConfig: ExampleJobConfig,
        private val exampleJobService: ExampleJobService
) {
    companion object : KLogging()

    @GetMapping("/api/example-job/execute")
    fun executeJob(): ExampleJobResult = exampleJobService
            .execute()
            .also(::logResponse)

    @GetMapping("/api/environment/jmespath/v1")
    fun environmentJmespathV1(@RequestParam q: String): JqResponse {
        val data: Any? = env.jmespath(q)
        return JqResponse(data = data)
                .also(::logResponse)
    }

    @GetMapping("/api/environment/jmespath/v2")
    fun environmentJmespathV2(@RequestParam q: String): JqResponse {
        val data: Any? = env.jq(query = q) { JSON.convertValue(it) }
        return JqResponse(data = data)
                .also(::logResponse)
    }

    @GetMapping("/api/environment/bind/v1")
    fun environmentBind(@RequestParam q: String): JqResponse {
        val data: Any? = env.decode(name = q) { JSON.convertValue(it) }
        return JqResponse(data = data)
                .also(::logResponse)
    }

    @GetMapping("/api/configs")
    fun configs(): Map<String, Any?> {
        val results: List<ExecResult> = listOf(
                ExecResult(q = "exampleJobConfig", result = exampleJobConfig),
                exec("app.example.job.delay") { q ->
                    env.decode(q) { JSON.convertValue<Duration>(it) }
                },
                exec("app.example.job.items") { q ->
                    env.decode(q) { JSON.convertValue<List<String>>(it) }
                },
                exec("[example.job.items]") { q ->
                    env.jq("app", q) {
                        JSON.convertValue<Any>(it)
                    }
                },
                exec("app.example.job.items") { q ->
                    env.jq(q) {
                        JSON.convertValue<Map<Any, String>>(it).values.toList()
                    }
                },
                exec("app.example2.job") { q ->
                    env.decode(q) { JSON.convertValue<JsonNode?>(it) }
                },
                exec("app.example.job") { q ->
                    env.decode(q) { JSON.convertValue<JsonNode>(it) }
                },
                exec("app.service.q-name") { q ->
                    env.decode(q) { JSON.convertValue<JsonNode>(it) }
                },
                exec("app.example.job.foo") { q ->
                    env.decode(q) { JSON.convertValue<JsonNode?>(it) }
                }
                /*
                ,
                exec("app.service.q-name") { q ->
                    env.jq(q) { JSON.convertValue<JsonNode>(it) }
                }
                */
        )
        val response = mapOf(
                "results" to results
        )

        return response.also(::logResponse)
    }


    private fun logResponse(response: Any?) {
        logger.info { "response: $response" }
        logger.info { "response (json): ${JSON.encode(response)}" }
    }
}

private val JSON = Jackson.defaultMapper()

data class JqResponse(val data: Any?)

data class ExecResult(val q: String, val result: Any?)

fun exec(q: String, block: (String) -> Any?): ExecResult {
    return ExecResult(q = q, result = block(q))
}