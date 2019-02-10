package com.example.demo.exampleJob

import com.example.demo.util.spring.binder.jmespath
import mu.KLogging
import org.springframework.context.annotation.Bean
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component
import java.time.Duration

data class ExampleJobConfig(
        val enabled: Boolean,
        val min: Int,
        val max: Int,
        val delay: Duration,
        val items: Map<String, String>
) {
    fun items(): List<String> = items.values.toList()
}

@Component
class ExampleJobConfiguration(private val env: Environment) {
    companion object : KLogging()

    @Bean
    fun exampleJobConfig(): ExampleJobConfig {
        val query = "app.example.job"

        return env
                .jmespath<ExampleJobConfig>(query)
                .also { logger.info { "Got query=$query -> $it" } }
    }
}