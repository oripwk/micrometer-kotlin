package com.oripwk.micrometer.kotlin

import io.micrometer.core.instrument.Clock
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tag
import io.micrometer.core.instrument.Timer
import io.micrometer.core.instrument.Timer.Builder
import io.micrometer.core.instrument.distribution.pause.PauseDetector
import java.time.Duration
import java.util.concurrent.TimeUnit


/**
 * Measures the time taken for short tasks and the count of these tasks.
 *
 * @param name The base metric name
 * @param tags MUST be an even number of arguments representing key/value pairs of tags.
 * @return A new or existing timer.
 */
fun MeterRegistry.coTimer(name: String, vararg tags: String): CoroutineTimer =
        CoroutineTimer(timer(name, * tags), this.config().clock())

/**
 * Measures the time taken for short tasks and the count of these tasks.
 *
 * @param name The base metric name
 * @param tags Sequence of dimensions for breaking down the name.
 * @return A new or existing timer.
 */
fun MeterRegistry.coTimer(name: String, tags: Iterable<Tag>): CoroutineTimer =
        CoroutineTimer(timer(name, tags), this.config().clock())

/**
 * @param tags Tags to add to the eventual timer.
 * @param publishPercentiles Produces an additional time series for each requested percentile. This percentile
 * is computed locally, and so can't be aggregated with percentiles computed across other
 * dimensions (e.g. in a different instance). Use [publishPercentileHistogram]
 * to publish a histogram that can be used to generate aggregable percentile approximations.
 * @param percentilePrecision Determines the number of digits of precision to maintain on the dynamic range histogram used to compute
 * percentile approximations. The higher the degrees of precision, the more accurate the approximation is at the
 * cost of more memory.
 * @param publishPercentileHistogram Adds histogram buckets used to generate aggregable percentile approximations in monitoring
 * systems that have query facilities to do so (e.g. Prometheus' `histogram_quantile`,
 * Atlas' `percentiles`.
 * @param sla Publish at a minimum a histogram containing your defined SLA boundaries. When used in conjunction with
 * [publishPercentileHistogram], the boundaries defined here are included alongside
 * other buckets used to generate aggregable percentile approximations.
 * @param minimumExpectedValue Sets the minimum value that this timer is expected to observe. Sets a lower bound
 * on histogram buckets that are shipped to monitoring systems that support aggregable percentile approximations.
 * @param maximumExpectedValue Sets the maximum value that this timer is expected to observe. Sets an upper bound
 * on histogram buckets that are shipped to monitoring systems that support aggregable percentile approximations.
 * @param distributionStatisticExpiry Statistics emanating from a timer like max, percentiles, and histogram counts decay over time to
 * give greater weight to recent samples (exception: histogram counts are cumulative for those systems that expect cumulative
 * histogram buckets). Samples are accumulated to such statistics in ring buffers which rotate after
 * this expiry, with a buffer length of [distributionStatisticBufferLength]
 * @param distributionStatisticBufferLength Statistics emanating from a timer like max, percentiles, and histogram counts decay over time to
 * give greater weight to recent samples (exception: histogram counts are cumulative for those systems that expect cumulative
 * histogram buckets). Samples are accumulated to such statistics in ring buffers which rotate after
 * [distributionStatisticExpiry], with this buffer length.
 * @param pauseDetector Sets the pause detector implementation to use for this timer. Can also be configured on a registry-level with
 * [MeterRegistry.pauseDetector]
 * @param description Description text of the eventual timer.
 */
fun Builder.coTimer(
    meterRegistry: MeterRegistry,
    tags: List<Tag> = emptyList(),
    publishPercentiles: List<Double>? = null,
    percentilePrecision: Int? = null,
    publishPercentileHistogram: Boolean? = null,
    sla: List<Duration>? = null,
    minimumExpectedValue: Duration? = null,
    maximumExpectedValue: Duration? = null,
    distributionStatisticExpiry: Duration? = null,
    distributionStatisticBufferLength: Int? = null,
    pauseDetector: PauseDetector? = null,
    description: String? = null
    ): CoroutineTimer {
        val timer = this
                .tags(tags)
                .also { if (publishPercentiles != null) it.publishPercentiles(* publishPercentiles.toDoubleArray()) }
                .percentilePrecision(percentilePrecision)
                .publishPercentileHistogram(publishPercentileHistogram)
                .also { if (sla != null) it.sla(* sla.toTypedArray()) }
                .minimumExpectedValue(minimumExpectedValue)
                .maximumExpectedValue(maximumExpectedValue)
                .distributionStatisticExpiry(distributionStatisticExpiry)
                .distributionStatisticBufferLength(distributionStatisticBufferLength)
                .pauseDetector(pauseDetector)
                .description(description)
                .register(meterRegistry)
        return CoroutineTimer(timer, meterRegistry.config().clock())
    }

class CoroutineTimer internal constructor(timer: Timer, private val clock: Clock) : Timer by timer {
    /**
     * Executes the suspend function [f] and records the time taken.
     *
     * @param f Suspend function to execute and measure the execution time.
     * @param T The return type of [f]
     * @return The return value of [f].
     * @throws Exception Any exception bubbling up from the callable.
     */
    suspend fun <T> record(f: suspend () -> T): T {
        val s = clock.monotonicTime()
        try {
            return f()
        } finally {
            val e = clock.monotonicTime()
            record(e - s, TimeUnit.NANOSECONDS)
        }
    }

    /**
     * Wrap a suspend function so that it is timed when invoked.
     *
     * @param f The suspend function to time when it is invoked.
     * @param T The return type of [f]
     * @return The wrapped suspend function.
     */
    fun <T> wrap(f: suspend () -> T): suspend () -> T = { record(f) }
}

