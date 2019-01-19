package com.oripwk.micrometer.kotlin

import io.micrometer.core.instrument.MockClock
import io.micrometer.core.instrument.Tag
import io.micrometer.core.instrument.Timer
import io.micrometer.core.instrument.simple.SimpleConfig
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals

object Spec : Spek({

    val clock = MockClock()
    val registry = SimpleMeterRegistry(SimpleConfig.DEFAULT, clock)

    beforeEachTest {
        registry.forEachMeter { registry.remove(it) }
    }

    describe("A coroutine timer") {
        it("should record time correctly") {
            val timer = registry.coTimer("timer")
            runBlocking {
                timer.record { GlobalScope.async {  }.await() }
                val actual = registry.meters.single() as Timer
                assertEquals(1, actual.count())
                assertEquals(0, actual.totalTime(TimeUnit.NANOSECONDS).toLong())
            }
        }
        it("should have the correct name and tags") {
            val timer = registry.coTimer("timer", listOf(Tag.of("key", "value")))
            runBlocking {
                timer.record { GlobalScope.async {  }.await() }
                val actual = registry.meters.single() as Timer
                assertEquals("timer", actual.id.name)
                assertEquals(Tag.of("key", "value"), actual.id.tags.single())
            }
        }

        it("should have the correct name and tags also with vararg") {
            val timer = registry.coTimer("timer", "key", "value")
            runBlocking {
                timer.record { GlobalScope.async {  }.await() }
                val actual = registry.meters.single() as Timer
                assertEquals("timer", actual.id.name)
                assertEquals(Tag.of("key", "value"), actual.id.tags.single())
            }
        }

        it("should work correctly also with Builder") {
            val timer = Timer.builder("timer").coTimer(registry, tags = listOf(Tag.of("key", "value")))
            runBlocking {
                timer.record { GlobalScope.async {  }.await() }
                val actual = registry.meters.single() as Timer
                assertEquals("timer", actual.id.name)
                assertEquals(Tag.of("key", "value"), actual.id.tags.single())
            }
        }

        it("should work correctly also with Builder and optional varargs") {
            val timer = Timer
                    .builder("timer")
                    .coTimer(
                            meterRegistry = registry,
                            tags = listOf(Tag.of("key", "value")),
                            publishPercentiles = listOf(1.0)
                    )
            runBlocking {
                timer.record { GlobalScope.async {  }.await() }
                assertEquals(2, registry.meters.size)
                val actualTimer = registry.find("timer").timer()!!
                val actualPercentile = registry.find("timer.percentile").gauge()!!
                assertEquals("timer", actualTimer.id.name)
                assertEquals(Tag.of("key", "value"), actualTimer.id.tags.single())
                assertEquals(setOf(Tag.of("key", "value"), Tag.of("phi", "1")), actualPercentile.id.tags.toSet())
            }
        }

        it("should record time correctly") {
            val timer = registry.coTimer("timer")
            runBlocking {
                val wrapped = timer.wrap { GlobalScope.async { 42 }.await() }
                val result = wrapped()
                assertEquals(42, result)
                val actual = registry.meters.single() as Timer
                assertEquals(1, actual.count())
                assertEquals(0, actual.totalTime(TimeUnit.NANOSECONDS).toLong())
            }
        }

    }
})