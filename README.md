# KMPCleanArchSample

Пример мультиплатформенного приложения (Android + iOS) на Kotlin Multiplatform и
Compose Multiplatform, организованного по принципам чистой архитектуры с разбивкой
на Gradle-модули по слоям и фичам.

Демо-функциональность: простой трекер задач — список активных задач и история
выполненных.

## Стек технологий

| Назначение          | Библиотека                              |
|----------------------|------------------------------------------|
| UI                   | Compose Multiplatform 1.11, Material 3   |
| Навигация            | Navigation 3 (`NavDisplay`, back stack на вкладку) |
| Состояние экранов     | `androidx.lifecycle.ViewModel` (KMP), привязан к записи back stack |
| DI                    | Koin                                     |
| Локальная БД          | Room (KMP-сборка `androidx.room3`) + SQLite (bundled) |
| Сеть                  | Ktor Client (OkHttp на Android, Darwin на iOS) |
| Сериализация          | kotlinx.serialization                    |
| Асинхронность         | kotlinx.coroutines                       |
| Kotlin                | 2.4.10                                   |

Версии всех зависимостей зафиксированы в [gradle/libs.versions.toml](gradle/libs.versions.toml).

## Использование как шаблона

Перед началом работы над новым проектом на основе этого репозитория замените
базовый пакет `ru.marwinka.kmpcleanarchsample` и имя `KMPCleanArchSample` одной
командой:

```bash
./scripts/rename-template.sh com.acme.myapp AcmeApp
```

Без аргументов скрипт спросит новые значения интерактивно; `--dry-run` покажет,
что изменится, ничего не трогая. Скрипт требует чистое git-дерево (коммитит или
откладывает свои изменения перед запуском) и сам прогоняет `ktlintFormat` в конце,
т.к. смена пакета почти всегда меняет алфавитный порядок импортов.

Что скрипт **не** трогает: `TEAM_ID`/подпись в
[iosApp/Configuration/Config.xcconfig](iosApp/Configuration/Config.xcconfig) (это
вы настраиваете сами под свой Apple Developer аккаунт) и внутренние ID
convention-плагинов в `build-logic` (`kmpcleanarchsample.kmp.library`/`.kmp.compose`)
— это чисто механика сборки, наружу не влияет.

## Структура модулей

```
androidApp/            Android-приложение (Activity, DI-старт)
iosApp/                 iOS-приложение (SwiftUI-обёртка)
shared/                 Composable App(), сборка Koin-модулей, Room AppDatabase, iOS entry point
build-logic/            Gradle convention-плагины (общие настройки KMP-модулей)

core/                   Общие утилиты: DispatcherProvider, Clock, AppError, AppResult
core/network/           Ktor HttpClient (платформенные engine — OkHttp/Darwin)
core/database/          DatabasePathProvider (платформенный путь к файлу БД)

design-system/          Общие Compose-компоненты и тема (AppTheme, TaskCard)

feature/tasks/           Фича "Задачи" — 4 отдельных Gradle-модуля и контракт для других фич:
  ├── api/                CompletedTasksSource — единственное, что фичи видят друг у друга
  ├── domain/            модели, интерфейс репозитория, use case'ы — без Android/Compose/Room
  ├── data/               репозиторий, Room Entity/DAO фичи (local/), TaskApi + мок (remote/)
  ├── presentation/       ViewModel, Route/Content (+ @Preview), NavDisplay вкладки
  └── di/                 Koin-модуль, связывающий domain/data/presentation
feature/history/         Фича "История" — та же структура из 4 модулей; данные получает через feature/tasks/api
```

Каждый слой фичи — это отдельный Gradle-модуль, а не просто пакет. Границы
Clean Architecture проверяются компилятором: `presentation` физически не может
импортировать Room (нет такой зависимости в classpath), а `domain` не видит
ни Android, ни Compose, ни Room/Ktor.

### Граф зависимостей между модулями

```
androidApp ─▶ shared ─┬─▶ feature:tasks:presentation ─▶ feature:tasks:domain ─▶ core
                       ├─▶ feature:tasks:di ─┬─▶ feature:tasks:data ─▶ feature:tasks:api
                       │                      └─▶ feature:tasks:presentation
                       ├─▶ feature:tasks:data   (Room-сущности для AppDatabase)
                       ├─▶ feature:history:presentation ─▶ feature:history:domain
                       ├─▶ feature:history:di ─▶ feature:history:data ─▶ feature:tasks:api
                       ├─▶ core:network, core:database
                       └─▶ design-system
```

`core`, `core:network`, `core:database` — платформенно-независимые "нижние" слои
без зависимостей от фич. Каждая фича хранит свои Room-сущности и DAO в собственном
`:data`, а общий `AppDatabase` объявлен в `shared`: только точка сборки может видеть
сущности всех фич. `feature:*` — самостоятельные вертикали, видящие друг друга только
через `:api`. `shared` собирает все Koin-модули (из `:di`-модулей фич и
`appDatabaseModule`) и рисует `App()` с вкладками (из `:presentation`-модулей фич).

Эти правила не только описаны, но и проверяются: корневой `build.gradle.kts` на этапе
конфигурации проверяет зависимости между проектами и роняет сборку при нарушении.

## Архитектура внутри фичи (`feature/tasks`, `feature/history`)

Каждая фича — 4 Gradle-модуля по слоям чистой архитектуры:

- **`:domain`** — модели (`Task`), интерфейс репозитория (`TaskRepository`),
  use case'ы (`GetActiveTasksUseCase`, `RefreshTasksUseCase`, `CompleteTaskUseCase`).
  Зависит только от `core` — компилятор физически не пустит сюда Room/Ktor/Compose.
- **`:data`** — `TaskRepositoryImpl` (публичный класс — на него по имени ссылается `:di`,
  а `internal` не пересекает границы Gradle-модулей), который мапит `TaskEntity` (Room)
  в domain-модель `Task` и дёргает `TaskApi` для наполнения БД.
- **`:presentation`** — `ViewModel` со `StateFlow<UiState>` (собирается через `stateIn`),
  stateful `TasksRoute` (берёт ViewModel из Koin) и stateless `TasksContent` с `@Preview`,
  ключи навигации и `NavDisplay` вкладки. Каждая запись back stack получает свой
  `ViewModelStore`. Зависит только от `:domain` — не видит `:data` и Room/Ktor вообще.
- **`:di`** — Koin-модуль фичи, единственное место, где интерфейс репозитория из
  `:domain` связывается с реализацией из `:data`.

`feature:history` получает выполненные задачи через контракт `feature:tasks:api`
(`CompletedTasksSource`) и маппит их в свою модель `TaskHistoryEntry` — фичи не видят
таблицы и domain-модели друг друга.

## Добавление новой фичи

Скелет из 4 модулей (`domain/data/presentation/di`) генерируется таском `newFeature`
в корневом [build.gradle.kts](build.gradle.kts):

```bash
./gradlew newFeature -PfeatureName=<FEATURE_NAME>
```

Таск создаёт папки и `build.gradle.kts` для каждого слоя (по образцу `feature/tasks`),
кладёт в каждый слой `README.md` с описанием его ответственности и дописывает
`include(...)` в [settings.gradle.kts](settings.gradle.kts). Дальше — вручную:
наполнить `domain`, реализовать репозиторий в `data` (источник данных не угадывается
автоматически, в `data/build.gradle.kts` останется `TODO`), написать `presentation`,
собрать `di`-модуль и подключить `:presentation` + `:di` в `shared`.

## Данные

`TaskApi` — заглушка (`FakeTaskApi`): при первом запуске отдаёт 4 фиксированные
задачи с искусственной задержкой 400 мс. `TaskRepositoryImpl.refresh()` подгружает
их в Room только если таблица пуста, дальше приложение работает целиком с локальной БД.
Реального REST-эндпоинта нет, хотя слой `core:network` с настроенным Ktor-клиентом
уже подготовлен для подключения.

## Тесты

Юнит-тесты лежат в `commonTest` и гоняются на `iosSimulatorArm64Test` (JVM-таргета
у чисто общих модулей нет):

- `core`: `DispatcherProviderTest`
- `feature/tasks`: `TaskUseCasesTest`, `TasksViewModelTest`, `TasksFeatureModuleTest` (граф Koin)
- `feature/history`: `GetTaskHistoryUseCaseTest`, `TaskHistoryRepositoryImplTest`

```bash
./gradlew allTests
```

## Сборка

```bash
# Android
./gradlew :androidApp:assembleDebug

# Проверка общего кода без таргет-специфичной компиляции
./gradlew compileKotlinMetadata
```

iOS-приложение собирается и запускается через Xcode-проект в [iosApp](iosApp).
