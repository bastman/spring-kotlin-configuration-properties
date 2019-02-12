package com.example.demo

import com.example.demo.exampleJob.ExampleJobConfig
import com.example.demo.util.runtime.RuntimeStats
import com.example.demo.util.spring.binder.jmespath
import com.fasterxml.jackson.annotation.JsonProperty
import mu.KLogging
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.boot.runApplication
import org.springframework.context.ApplicationListener
import org.springframework.core.env.Environment
import java.lang.management.RuntimeMXBean
import java.time.Instant
import java.util.*
import javax.annotation.PostConstruct

fun main(args: Array<String>) {
    runApplication<ExampleApplication>(*args)
}

@SpringBootApplication
class ExampleApplication(
        private val env: Environment,
        private val exampleJobConfig: ExampleJobConfig
) : ApplicationListener<ApplicationReadyEvent> {
    companion object : KLogging(), RuntimeStats

    @PostConstruct
    internal fun started() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
        logger.info { bootLogMessage("==== APP STARTING ... ====") }
        System.gc()

    }

    override fun onApplicationEvent(contextRefreshedEvent: ApplicationReadyEvent) {
        System.gc()
        logger.info { bootLogMessage("==== APP READY ====") }
    }


    private fun bootLogMessage(headline: String): String {
        val envName: EnvName = env.jmespath("app.envName")
        val serviceInfo: ServiceInfo = env.jmespath("app.service")
        val mx: RuntimeMXBean = runtimeMxBean()
        val systemProps = mx.systemProperties.toMap()
                .filterKeys { it !in listOf("java.class.path") }

        return """
        | === $headline
        | - envName: $envName
        | - service: ${serviceInfo.qualifiedName}
        | - exampleJobConfig: $exampleJobConfig
        | - charset: ${defaultCharset()}
        | - timezone: ${defaultTimezone().id} (now=${Instant.now()})
        | - memory heap stats (in MB): ${memoryStatsInMegaBytes()}
        | - processors: ${availableProcessors()}
        | - mx.inputArgs: ${mx.inputArguments}
        | - mx.systemProps: $systemProps
        | === """.trimMargin()
    }

}

private enum class EnvName {
    @JsonProperty("dev")
    DEV,
    @JsonProperty("prod")
    PROD
}

private data class ServiceInfo(val name: String, val qualifiedName: String)
