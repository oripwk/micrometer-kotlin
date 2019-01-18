# Kotlin for Micrometer
Kotlin support for micrometer.io

## Easily create timers that can receive suspend functions

```kotlin
    val meterRegistry = SimpleMeterRegistry()
    val myTimer = meterRegistry.coTimer("my-timer")
    
    val result: Int = myTimer.record {
        GlobalScope.async { 42 }.await()
    }
```

## Comprehensive `Timer` building support

The API is fully compatible with `Timer.builder(…)` API, leveraging Kotlin's default parameters constructor:

```kotlin
    Timer
            .builder("my-timer")
            .coTimer(
                meterRegistry = SimpleMeterRegistry(),
                tags = listOf(Tag.of("env", "dev")),
                maximumExpectedValue = Duration.ofSeconds(1)
                // …
            )
```
