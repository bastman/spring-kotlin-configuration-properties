package com.example.demo

import mu.KLogging
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.boot.runApplication
import org.springframework.context.ApplicationListener
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
        logger.info { "started." }
        System.gc()

    }

    override fun onApplicationEvent(contextRefreshedEvent: ApplicationReadyEvent) {
        System.gc()
        logger.info { "ready." }
    }
}