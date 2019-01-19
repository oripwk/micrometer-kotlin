# Kotlin for Micrometer
Kotlin support for micrometer.io

## Install

```gradle
repositories {
    maven { url "https://ori.bintray.com/maven" }
}
```

### Kotlin 1.3

```gradle
dependencies {
    implementation group: 'io.oripwk', name: 'micrometer-kotlin', version: '0.1'
}
```

### Kotlin 1.2

```gradle
dependencies {
    implementation group: 'io.oripwk', name: 'micrometer-kotlin', version: '0.1.kotlin12'
}
```

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
