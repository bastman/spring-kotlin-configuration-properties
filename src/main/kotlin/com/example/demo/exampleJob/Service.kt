package com.example.demo.exampleJob

import mu.KLogging
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.*

data class ExampleJobResult(val jobId: UUID, val executedAt: Instant)

@Component
class ExampleJobService(private val config: ExampleJobConfig) {
    companion object : KLogging()

    fun execute(): ExampleJobResult {
        return ExampleJobResult(jobId = UUID.randomUUID(), executedAt = Instant.now())
                .also { logger.info { "execute() - using config: $config - resturns result: $it" } }

    }

}