package com.example.demo.rest

import com.example.demo.config.Jackson
import com.example.demo.config.encode
import com.example.demo.exampleJob.ExampleJobConfig
import com.example.demo.util.spring.binder.jmespath
import mu.KLogging
import org.springframework.core.env.Environment
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController


@RestController
class ApiController(
        private val env: Environment,
        private val exampleJobConfig: ExampleJobConfig
) {
    companion object : KLogging()

    @GetMapping("/api/environment")
    fun environment(@RequestParam jq: String): JqResponse {
        val data: Any? = env.jmespath(jq)
        return JqResponse(data = data)
                .also(::logResponse)
    }

    @GetMapping("/api/configs")
    fun configs(): Map<String, Any?> =
            mapOf(
                    "exampleJobConfig" to exampleJobConfig
            ).also(::logResponse)

    private fun logResponse(response: Any?) {
        logger.info { "response: $response" }
        logger.info { "response (json): ${JSON.encode(response)}" }
    }
}

private val JSON = Jackson.defaultMapper()

data class JqResponse(val data: Any?)