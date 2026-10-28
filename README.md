[![CI](https://github.com/Romanow/feature-flags/actions/workflows/build.yml/badge.svg?branch=master)](https://github.com/Romanow/feature-flags/actions/workflows/build.yml)
[![pre-commit](https://img.shields.io/badge/pre--commit-enabled-brightgreen?logo=pre-commit)](https://github.com/pre-commit/pre-commit)
[![Release](https://img.shields.io/github/v/release/Romanow/feature-flags?logo=github&sort=semver)](https://github.com/Romanow/feature-flags/releases/latest)
[![Codecov](https://codecov.io/gh/Romanow/feature-flags/branch/master/graph/badge.svg?token=Cckw6pHLh7)](https://codecov.io/gh/Romanow/feature-flags)
[![License](https://img.shields.io/github/license/Romanow/feature-flags)](https://github.com/Romanow/feature-flags/blob/master/LICENSE)

# Spring Boot Feature Flags

Библиотека для управления feature flags в Spring Boot: чтение значений из YAML, обновление полей `@Value` и `@Feature` и
переключение реализаций во время работы приложения без перезапуска контекста. Флаги могут содержать логические, числовые
и строковые значения.

Проект написан на Kotlin, собирается для JVM 21 и использует Spring Boot 3.5.16.

## Подключение

Автоконфигурация
[`FeatureFlagsAutoConfiguration`](src/main/kotlin/ru/romanow/feature/flags/FeatureFlagsAutoConfiguration.kt)
подключается автоматически вместе с зависимостью.

### Maven

```xml

<dependency>
    <groupId>ru.romanow-alex</groupId>
    <artifactId>feature-flags</artifactId>
    <version>${feature-flags.version}</version>
</dependency>
```

### Gradle

```groovy
implementation "ru.romanow-alex:feature-flags:$featureFlagsVersion"
```

## Использование

Флаги объявляются в файле `features.yml` в `src/main/resources`:

```yaml
features:
    new-checkout: false
    checkout-provider: legacy
    max-items: 10
```

Для изменения значений во время работы используется внешний файл `/opt/features.yml` с такой же структурой. Его значения
переопределяют одноимённые значения из classpath; остальные сохраняются. Если внешнего файла нет, используются только
значения из classpath. По умолчанию библиотека проверяет изменения каждые 5 секунд (параметр
`features-config.reload-interval`).

### Поле с `@Feature`

```kotlin
import org.springframework.stereotype.Service
import ru.romanow.feature.flags.annotations.Feature

@Service
class CartService {
    @Feature(value = "max-items", defaultValue = "10")
    private var maxItems: Int = 10

    fun canAdd(items: Int): Boolean = items < maxItems
}
```

Имя в `@Feature` указывается без префикса `features.`. Значение преобразуется к типу поля средствами Spring. Если
свойства нет, используется `defaultValue`, а если он не задан — исходное значение поля. Это правило действует и при
удалении свойства во время перезагрузки.

Поля должны быть изменяемыми и нестатическими, на одном поле нельзя совмещать `@Feature` и `@Value`.

### Поле с `@Value`

Библиотека также обновляет поля со стандартной аннотацией Spring, если выражение содержит `${features.###}` (поля должны
быть объявлены как члены класса):

```kotlin
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class CheckoutService {
    @Value($$"${features.new-checkout:false}")
    private var newCheckout: Boolean = false

    fun isNewCheckoutEnabled(): Boolean = newCheckout
}
```

### Чтение из `Features`

```kotlin
import org.springframework.stereotype.Service
import ru.romanow.feature.flags.properties.Features

@Service
class FeatureService(private val features: Features) {
    fun isNewCheckoutEnabled(): Boolean = features["new-checkout"]?.toBoolean() ?: false
}
```

### Переключение реализаций

Реализации общего интерфейса регистрируются как Spring-бины. Условие выбора задаётся аннотацией
`@ConditionOnFeatureEnabled`, реализация по умолчанию — аннотацией `@DefaultFeatureImplementation`:

```kotlin
import org.springframework.stereotype.Service
import ru.romanow.feature.flags.annotations.ConditionOnFeatureEnabled
import ru.romanow.feature.flags.annotations.DefaultFeatureImplementation

interface Checkout {
    fun provider(): String
}

@Service
@ConditionOnFeatureEnabled(feature = "checkout-provider", expected = "modern")
class ModernCheckout : Checkout {
    override fun provider(): String = "modern"
}

@Service
@DefaultFeatureImplementation(feature = "checkout-provider")
class LegacyCheckout : Checkout {
    override fun provider(): String = "legacy"
}

@Service
class OrderService(private val checkout: Checkout) {
    fun checkoutProvider(): String = checkout.provider()
}
```

При `features.checkout-provider: modern` вызовы направляются в `ModernCheckout`, при другом значении или отсутствии
флага — в `LegacyCheckout`. После изменения файла существующий `OrderService` начинает использовать выбранную реализацию
через тот же прокси.

`expected` сравнивается со строковым значением флага; по умолчанию это `true`. Для одного флага можно задать несколько
реализаций с разными `expected` и не более одной реализации по умолчанию. Все реализации группы должны иметь общий
интерфейс; зависимость внедряется по этому интерфейсу. Если подходящей реализации нет и реализация по умолчанию не
задана, выбор завершается ошибкой.

Переключаемые реализации должны иметь scope `singleton`. Другие scopes, включая `prototype`, `request` и `session`,
отклоняются при сборке контекста.

Каждая реализация создаётся при первом выборе и сохраняется до закрытия контекста. При повторном выборе используется
тот же экземпляр с накопленным состоянием. Переключение не освобождает ресурсы неактивных реализаций: их callbacks
уничтожения вызываются Spring при закрытии контекста.

Прокси предоставляет общие бизнес-интерфейсы реализаций. Служебные интерфейсы `Aware`, `InitializingBean`,
`DisposableBean`, `SmartInitializingSingleton`, `Lifecycle`, `Phased`, `AutoCloseable` и их наследники исключаются.
Бизнес-интерфейс должен быть независим от этих контрактов: например, `Checkout` и `AutoCloseable` нужно реализовывать
отдельно на классе. Если общего бизнес-интерфейса нет, библиотека выдаёт ошибку при сборке контекста.

## Конфигурация

Настройки библиотеки задаются в `application.yml`:

```yaml
features-config:
    enabled: true
    location: /opt/features.yml
    reload-enabled: true
    reload-interval: PT5S
    reload-init-delay: PT5S
```

| Свойство                            | По умолчанию                                    | Назначение                                           |
|-------------------------------------|-------------------------------------------------|------------------------------------------------------|
| `features-config.enabled`           | `${FEATURES_ENABLED:true}`                      | Включает автоконфигурацию библиотеки.                |
| `features-config.location`          | `${FEATURES_CONFIG_LOCATION:/opt/features.yml}` | Путь к внешнему YAML-файлу в файловой системе        |
| `features-config.reload-enabled`    | `${FEATURES_RELOAD_ENABLED:true}`               | Включает периодическую перезагрузку                  |
| `features-config.reload-interval`   | `${FEATURES_RELOAD_INTERVAL:PT5S}`              | Пауза между завершением проверки и началом следующей |
| `features-config.reload-init-delay` | `${FEATURES_RELOAD_INTERVAL:PT5S}`              | Задержка перед первой проверкой                      |

Интервалы имеют тип `Duration`; в примере используется формат ISO 8601 (`PT5S` — 5 секунд).

При `reload-enabled: false` начальная загрузка сохраняется. Обновление можно вызвать вручную через Spring bean
[`FeatureMonitor`](src/main/kotlin/ru/romanow/feature/flags/services/FeatureMonitor.kt), вызвав `refresh()`.
При `enabled: false` автоконфигурация, включая `Features`, обработчики и монитор, не создаётся.

## Реализация

* [`FeatureFlagsAutoConfiguration`](src/main/kotlin/ru/romanow/feature/flags/FeatureFlagsAutoConfiguration.kt) загружает
  настройки и объединяет `classpath:features.yml` с внешним файлом в источник свойств Spring `Environment` с именем
  `features`.
* [`FeatureFieldBeanPostProcessor`](src/main/kotlin/ru/romanow/feature/flags/config/FeatureFieldBeanPostProcessor.kt)
  запоминает поля с `@Feature` и подходящими `@Value`, преобразует значения и обновляет поля уже созданных beans.
* [`FeatureImplementationBeanFactoryPostProcessor`](src/main/kotlin/ru/romanow/feature/flags/config/FeatureImplementationBeanFactoryPostProcessor.kt)
  группирует реализации по флагу и создаёт JDK-прокси с общими бизнес-интерфейсами. Реализации сохраняются как обычные
  Spring singleton до закрытия контекста. При переключении меняется только target прокси; созданные реализации,
  сам прокси и зависимые сервисы не уничтожаются. Блокировка чтения/записи согласует вызовы прокси и выбор реализации.
* [`FeatureMonitor`](src/main/kotlin/ru/romanow/feature/flags/services/FeatureMonitor.kt) по расписанию перечитывает
  YAML. Если свойства изменились, заменяет источник в `Environment`, обновляет карту `Features`, затем поля и
  реализации.
* Планировщик регистрируется автоконфигурацией; `@EnableScheduling` в приложении не требуется.
