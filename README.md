[![CI](https://github.com/Romanow/feature-flags/actions/workflows/build.yml/badge.svg?branch=master)](https://github.com/Romanow/feature-flags/actions/workflows/build.yml)
[![pre-commit](https://img.shields.io/badge/pre--commit-enabled-brightgreen?logo=pre-commit)](https://github.com/pre-commit/pre-commit)
[![Release](https://img.shields.io/github/v/release/Romanow/feature-flags?logo=github&sort=semver)](https://github.com/Romanow/feature-flags/releases/latest)
[![Codecov](https://codecov.io/gh/Romanow/feature-flags/branch/master/graph/badge.svg?token=Cckw6pHLh7)](https://codecov.io/gh/Romanow/feature-flags)
[![License](https://img.shields.io/github/license/Romanow/feature-flags)](https://github.com/Romanow/feature-flags/blob/master/LICENSE)

# Feature Flags

## Подключение

### Maven

```xml

<dependency>
    <groupId>ru.romanow</groupId>
    <artifactId>feature-flags</artifactId>
    <version>${feature-flags.version}</version>
</dependency>
```

### Gradle

```groovy
testImplementation "ru.romanow:feature-flags:$featureFlagsVersion"
```

## Реализация

Классы, которые необходимо перезагружать при обновлении флагов:

* Класс `Features` (`@ConfigurationProperties(prefix = "features")`) и beans, куда он autowired.
* Beans, помеченные аннотациями `@ConditionOnFeatureEnabled`, `ConditionOnFeatureDisabled` и
  `@DefaultFeatureImplementation`.
* Классы, где есть `@Value("${features.*})`.

Способы получения обновленного конфига:

* Файл на host-машине:
    * classpath (by default) – не контролируется;
    * `/opt/features.yml` (контролируется переменной `features.config.location`).
* Запросом к config-серверу.
