[ ![Download](https://api.bintray.com/packages/ori/maven/micrometer-kotlin/images/download.svg?version=0.1) ](https://bintray.com/ori/maven/micrometer-kotlin/0.1/link) [ ![Download](https://api.bintray.com/packages/ori/maven/micrometer-kotlin/images/download.svg?version=0.1.kotlin12) ](https://bintray.com/ori/maven/micrometer-kotlin/0.1.kotlin12/link)

# Kotlin for Micrometer
Kotlin support for [micrometer.io](micrometer.io).

## Install

```gradle
repositories {
    maven { url "https://ori.bintray.com/maven" }
}
```

```gradle
dependencies {
    implementation group: 'io.oripwk', name: 'micrometer-kotlin', version: '0.1'
    // OR
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
