package io.oripwk.micrometer.kotlin

import io.micrometer.core.instrument.Clock
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tag
import io.micrometer.core.instrument.Timer
import io.micrometer.core.instrument.distribution.pause.PauseDetector
import java.time.Duration
import java.util.concurrent.TimeUnit


fun MeterRegistry.coTimer(name: String, vararg tags: String): CoroutineTimer =
        CoroutineTimer(timer(name, * tags), this.config().clock())

fun MeterRegistry.coTimer(name: String, tags: Iterable<Tag>): CoroutineTimer =
        CoroutineTimer(timer(name, tags), this.config().clock())

class CoroutineTimer internal constructor(timer: Timer, private val clock: Clock) : Timer by timer {
    companion object {
        operator fun invoke(
                name: String,
                meterRegistry: MeterRegistry,
                tags: List<Tag> = emptyList(),
                publishPercentiles: List<Double>? = null,
                percentilePrecision: Int? = null,
                publishPercentileHistogram: Boolean? = null,
                sla: Duration? = null,
                minimumExpectedValue: Duration? = null,
                maximumExpectedValue: Duration? = null,
                distributionStatisticExpiry: Duration? = null,
                distributionStatisticBufferLength: Int? = null,
                pauseDetector: PauseDetector? = null,
                description: String? = null
        ): CoroutineTimer {
            val timer = Timer
                    .builder(name)
                    .tags(tags)
                    .also { if (publishPercentiles != null) it.publishPercentiles(* publishPercentiles.toDoubleArray()) }
                    .percentilePrecision(percentilePrecision)
                    .publishPercentileHistogram(publishPercentileHistogram)
                    .sla(sla)
                    .minimumExpectedValue(minimumExpectedValue)
                    .maximumExpectedValue(maximumExpectedValue)
                    .distributionStatisticExpiry(distributionStatisticExpiry)
                    .distributionStatisticBufferLength(distributionStatisticBufferLength)
                    .pauseDetector(pauseDetector)
                    .description(description)
                    .register(meterRegistry)
            return CoroutineTimer(timer, meterRegistry.config().clock())
        }
    }

    suspend fun <T> record(f: suspend () -> T): T {
        val s = clock.monotonicTime()
        try {
            return f()
        } finally {
            val e = clock.monotonicTime()
            record(e - s, TimeUnit.NANOSECONDS)
        }
    }
}

