package com.example.demo

import com.example.demo.config.Jackson
import com.example.demo.config.encode
import mu.KLogging
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.boot.runApplication
import org.springframework.context.ApplicationListener
import java.time.Instant
import java.util.*
import javax.annotation.PostConstruct

fun main(args: Array<String>) {
    println(":)")
    runApplication<ExampleApplication>(*args)
}

@SpringBootApplication
class ExampleApplication() : ApplicationListener<ApplicationReadyEvent> {
    companion object : KLogging()

    @PostConstruct
    internal fun started() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
        logger.info { "started. ${JSON.encode(Instant.now())}" }
        System.gc()

    }

    override fun onApplicationEvent(contextRefreshedEvent: ApplicationReadyEvent) {
        System.gc()
        logger.info { "ready. ${JSON.encode(Instant.now())}" }
    }
}

private val JSON = Jackson.defaultMapper()