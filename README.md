[![Build project](https://github.com/Romanow/feature-flags/actions/workflows/build.yml/badge.svg?branch=master)](https://github.com/Romanow/feature-flags/actions/workflows/build.yml)
[![pre-commit](https://img.shields.io/badge/pre--commit-enabled-brightgreen?logo=pre-commit)](https://github.com/pre-commit/pre-commit)

# Feature Flags

## Подключение

### Maven

```xml

<dependency>
    <groupId>ru.romanow.logging</groupId>
    <artifactId>log-masking-lib</artifactId>
    <version>${log-masking-lib.version}</version>
</dependency>
```

### Gradle

```groovy
testImplementation "ru.romanow.logging:log-masking-lib:$logMaskingVersion"
```

## Реализация

* Флаги по группам и именам.
* Дефолтное значение.
