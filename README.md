# Kotlin for Micrometer
[![Apache License](https://img.shields.io/badge/license-Apache%202-green.svg)](https://www.apache.org/licenses/LICENSE-2.0) [![Maven Central](https://img.shields.io/maven-central/v/com.oripwk/micrometer-kotlin.svg)](https://search.maven.org/artifact/com.oripwk/micrometer-kotlin/0.1/jar) [ ![Download](https://api.bintray.com/packages/bintray/jcenter/com.oripwk:micrometer-kotlin/images/download.svg?version=0.1) ](https://bintray.com/bintray/jcenter/com.oripwk:micrometer-kotlin/0.1/link)

Kotlin support for [micrometer.io](https://micrometer.io).

## Install

```gradle
repositories {
    mavenCentral() // jcenter()
}
```

```gradle
dependencies {
    implementation group: 'com.oripwk', name: 'micrometer-kotlin', version: '0.1'
    // OR
    implementation group: 'com.oripwk', name: 'micrometer-kotlin', version: '0.1.kotlin12'
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
