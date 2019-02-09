package com.example.demo.util.runtime

import java.lang.management.ManagementFactory
import java.lang.management.RuntimeMXBean
import java.nio.charset.Charset
import java.util.*

interface RuntimeStats {
    fun runtimeMxBean(): RuntimeMXBean = ManagementFactory.getRuntimeMXBean()
    fun memoryStatsInMegaBytes(): MemoryStats = MemoryStats.statsInMegaBytes()
    fun availableProcessors(): Int = Runtime.getRuntime().availableProcessors()
    fun defaultCharset(): Charset = Charset.defaultCharset()
    fun defaultTimezone(): TimeZone = TimeZone.getDefault()
}

data class MemoryStats(
        val total: Long,
        val free: Long,
        val max: Long,
        val used: Long
) {

    fun dividedBy(factor: Int): MemoryStats = copy(
            total = total / factor,
            free = free / factor,
            max = max / factor,
            used = used / factor
    )

    companion object {
        val KB: Int = 1024
        val MB: Int = KB * 1024

        fun stats(factor: Int = 1): MemoryStats {
            val total = Runtime.getRuntime().totalMemory()
            val free = Runtime.getRuntime().freeMemory()
            val used = total - free
            return MemoryStats(
                    total = total,
                    free = free,
                    max = Runtime.getRuntime().maxMemory(),
                    used = used
            ).dividedBy(factor = factor)
        }

        fun statsInMegaBytes(): MemoryStats = stats(factor = MB)
    }
}