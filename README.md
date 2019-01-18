# Kotlin for Micrometer
Kotlin support for micrometer.io

## Easily create timers that can receive suspend functions

```kotlin
    val meterRegistry = SimpleMeterRegistry()
    val myTimer = meterRegistry.coTimer("my-timer")
    
    val result = myTimer.record {
        GlobalScope.async { 42 }.await()
    }

    println(result)
```

## Comprehensive `Timer` building support

The API is fully compatibale with `Timer.builder(…)` API, using Kotlin's default paramerers constructor:

```kotlin
    val meterRegistry = SimpleMeterRegistry()
    val myTimer = meterRegistry.coTimer("my-timer")

    CoroutineTimer(
            name = "my-timer",
            meterRegistry = meterRegistry,
            tags = listOf(Tag.of("env", "dev")),
            maximumExpectedValue = Duration.ofSeconds(10)
    )

    // equivalent to:
    Timer
            .builder("my-timer")
            .tags(listOf(Tag.of("env", "dev")))
            .maximumExpectedValue(Duration.ofSeconds(10))
            .register(meterRegistry)
```
