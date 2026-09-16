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
| Навигация            | Voyager (Navigator, TabNavigator, ScreenModel) |
| DI                    | Koin                                     |
| Локальная БД          | Room (KMP-сборка `androidx.room3`) + SQLite (bundled) |
| Сеть                  | Ktor Client (OkHttp на Android, Darwin на iOS) |
| Сериализация          | kotlinx.serialization                    |
| Асинхронность         | kotlinx.coroutines                       |
| Kotlin                | 2.4.10                                   |

Версии всех зависимостей зафиксированы в [gradle/libs.versions.toml](gradle/libs.versions.toml).

## Структура модулей

```
androidApp/            Android-приложение (Activity, DI-старт)
iosApp/                 iOS-приложение (SwiftUI-обёртка)
shared/                 Composable App(), сборка Koin-модулей, iOS entry point
build-logic/            Gradle convention-плагины (общие настройки KMP-модулей)

core/                   Общие утилиты: DispatcherProvider, AppError, resultOf
core/network/           Ktor HttpClient (платформенные engine — OkHttp/Darwin)
core/database/          DatabasePathProvider (платформенный путь к файлу БД)

data/tasks/             Data-слой задач: Room DAO/Entity/Database, TaskApi (мок)

design-system/          Общие Compose-компоненты и тема (AppTheme, TaskCard)

feature/tasks/           Фича "Задачи" — 4 отдельных Gradle-модуля:
  ├── domain/            модели, интерфейс репозитория, use case'ы — без Android/Compose/Room
  ├── data/               реализация репозитория (Room/Ktor)
  ├── presentation/       ScreenModel, Screen, Tab (Compose + Voyager)
  └── di/                 Koin-модуль, связывающий domain/data/presentation
feature/history/         Фича "История" — та же структура из 4 модулей
```

Каждый слой фичи — это отдельный Gradle-модуль, а не просто пакет. Границы
Clean Architecture проверяются компилятором: `presentation` физически не может
импортировать Room (нет такой зависимости в classpath), а `domain` не видит
ни Android, ни Compose, ни Room/Ktor.

### Граф зависимостей между модулями

```
androidApp ─▶ shared ─┬─▶ feature:tasks:presentation ─▶ feature:tasks:domain ─▶ core
                       ├─▶ feature:tasks:di ─┬─▶ feature:tasks:data ─▶ data:tasks ─┬─▶ core:network
                       │                      └─▶ feature:tasks:presentation        └─▶ core:database
                       ├─▶ feature:history:presentation ─▶ feature:history:domain
                       ├─▶ feature:history:di ─▶ feature:history:data ─▶ data:tasks
                       └─▶ design-system
```

`core`, `core:network`, `core:database` — платформенно-независимые "нижние" слои
без зависимостей от фич. `data:tasks` объединяет сеть и БД в единый источник
данных. `feature:*` — самостоятельные вертикали, не зависящие друг от друга
напрямую. `shared` — точка сборки: собирает все Koin-модули (из `:di`-модулей
фич) и рисует `App()` с табами (из `:presentation`-модулей фич).

## Архитектура внутри фичи (`feature/tasks`, `feature/history`)

Каждая фича — 4 Gradle-модуля по слоям чистой архитектуры:

- **`:domain`** — модели (`Task`), интерфейс репозитория (`TaskRepository`),
  use case'ы (`GetActiveTasksUseCase`, `RefreshTasksUseCase`, `ToggleTaskDoneUseCase`).
  Зависит только от `core` — компилятор физически не пустит сюда Room/Ktor/Compose.
- **`:data`** — `TaskRepositoryImpl` (публичный класс — на него по имени ссылается `:di`,
  а `internal` не пересекает границы Gradle-модулей), который мапит `TaskEntity` (Room)
  в domain-модель `Task` и дёргает `TaskApi` для наполнения БД.
- **`:presentation`** — `ScreenModel` (Voyager) со `StateFlow<UiState>` и Composable-экран,
  который его отображает. Зависит только от `:domain` — не видит `:data` и, соответственно,
  Room/Ktor вообще.
- **`:di`** — Koin-модуль фичи, единственное место, где интерфейс репозитория из
  `:domain` связывается с реализацией из `:data`.

`feature:history` использует ту же таблицу (`TaskDao`) из `data:tasks`, но маппит
строки в свою собственную модель `TaskHistoryEntry` — фичи не делятся domain-моделями
между собой, только источником данных.

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
- `feature/tasks`: `TaskUseCasesTest`
- `feature/history`: `GetTaskHistoryUseCaseTest`

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
