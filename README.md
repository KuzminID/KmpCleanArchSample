[![CI](https://github.com/KuzminID/KmpCleanArchSample/actions/workflows/ci.yml/badge.svg)](https://github.com/KuzminID/KmpCleanArchSample/actions/workflows/ci.yml)

# KMPCleanArchSample

Пример мультиплатформенного приложения (Android + iOS) на Kotlin Multiplatform и
Compose Multiplatform, организованного по принципам чистой архитектуры с разбивкой
на Gradle-модули по слоям и фичам.

Демо-функциональность: трекер задач поверх [Rick and Morty API](https://rickandmortyapi.com/) —
список эпизодов к просмотру и история просмотренных.

Целевая архитектура обоих шаблонов (Android и KMP), выбор технологий по ситуациям, отклонения реализации от неё и подводные камни описаны в [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md).

## Стек технологий

| Назначение          | Библиотека                              |
|----------------------|------------------------------------------|
| UI                   | Compose Multiplatform 1.11, Material 3   |
| Навигация            | Navigation 3 (`NavDisplay`, back stack на вкладку) |
| Состояние экранов     | `androidx.lifecycle.ViewModel` (KMP), привязан к записи back stack |
| DI                    | Koin                                     |
| Локальная БД          | Room 3 (`androidx.room3`) + SQLite (bundled), своя база у каждой фичи |
| Сеть                  | Ktor Client (OkHttp на Android и JVM, Darwin на iOS) |
| Сериализация          | kotlinx.serialization                    |
| Асинхронность         | kotlinx.coroutines                       |
| Тесты                 | `kotlin.test`, `kotlinx-coroutines-test`, рукописные fake, Ktor `MockEngine` |
| Стиль и анализ        | ktlint, detekt                           |
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
shared/                 Composable App() с вкладками, сборка Koin-модулей, iOS entry point
build-logic/            Gradle convention-плагины (общие настройки KMP-модулей, JVM/Android/iOS-таргеты)

core/                   Общее: DispatcherProvider, Clock, Logger, AppError, AppResult, appResultOf
core/network/           Ktor HttpClient, networkResultOf — перевод сетевых ошибок в AppError
core/database/          DatabasePathProvider и общие настройки Room-builder'а
core/testing/           Тестовые утилиты: TestDispatcherProvider, FixedClock, RecordingLogger

design-system/          Тема и общие Compose-компоненты (AppTheme, TaskCard, AppTopBar, RefreshableContent)

feature/tasks/           Фича "Задачи":
  ├── api/                CompletedTasksSource — единственное, что фичи видят друг у друга
  ├── domain/            модели, интерфейс репозитория, use case'ы — без Android/Compose/Room
  ├── data/               internal-репозиторий, своя Room-база (local/), Ktor-клиент API (remote/), мапперы
  ├── presentation/       ViewModel, Route/Screen (+ превью состояний), NavDisplay вкладки
  ├── di/                 Koin-модуль фичи и тест графа
  └── testing/            FakeTaskRepository для тестов domain и presentation
feature/history/         Фича "История" — api/testing не нужны; данные получает через feature/tasks/api
```

Каждый слой фичи — это отдельный Gradle-модуль, а не просто пакет. Границы
Clean Architecture проверяются сборкой: `presentation` физически не может
импортировать Room (нет такой зависимости в classpath), а `domain` не видит
ни Android, ни Compose, ни Room/Ktor.

### Граф зависимостей между модулями

```
androidApp ─▶ shared ─┬─▶ feature:tasks:presentation ─▶ feature:tasks:domain ─▶ core
                       ├─▶ feature:tasks:di ─┬─▶ feature:tasks:data ─┬─▶ feature:tasks:domain, feature:tasks:api
                       │                      │                       └─▶ core:network, core:database
                       │                      └─▶ feature:tasks:presentation
                       ├─▶ feature:history:presentation ─▶ feature:history:domain
                       ├─▶ feature:history:di ─▶ feature:history:data ─▶ feature:tasks:api
                       ├─▶ core:network, core:database
                       └─▶ design-system
```

`core`, `core:network`, `core:database` — платформенно-независимые "нижние" слои
без зависимостей от фич. У каждой фичи своя Room-база в её `:data`: Entity, DAO и
реализации объявлены `internal`, а наружу `:data` отдаёт только Koin-модуль.
`feature:*` — самостоятельные вертикали, видящие друг друга только через `:api`.
`shared` собирает все Koin-модули (из `:di`-модулей фич) и рисует `App()` с вкладками
(из `:presentation`-модулей фич).

Эти правила не только описаны, но и проверяются: корневой `build.gradle.kts` на этапе
конфигурации проверяет зависимости между проектами и внешние библиотеки слоёв
(например, Room, Ktor или Koin в `domain`) и роняет сборку при нарушении.

## Архитектура внутри фичи (`feature/tasks`, `feature/history`)

Каждая фича — 4 Gradle-модуля по слоям чистой архитектуры:

- **`:domain`** — модели (`Task`), интерфейс репозитория (`TaskRepository`),
  use case'ы (`GetActiveTasksUseCase`, `RefreshTasksUseCase`, `CompleteTaskUseCase`).
  Зависит только от `core` — сборка не пустит сюда Room/Ktor/Compose/Koin.
- **`:data`** — `internal` `TaskRepositoryImpl`, своя база `TasksDatabase`, `KtorTaskApi`
  и мапперы DTO → Entity → domain в отдельном файле. Наружу видна только декларация
  `tasksDataModule` (Koin), которую подключает `:di`.
- **`:presentation`** — `ViewModel` с `uiState: StateFlow<UiState>` (собирается через
  `combine` + `stateIn`) и методами `onXxx`, stateful `TasksRoute` (берёт ViewModel из Koin)
  и stateless `TasksScreen(uiState, onXxx, modifier)` с превью каждого состояния,
  `@Serializable`-ключи навигации и `NavDisplay` вкладки. Каждая запись back stack получает
  свой `ViewModelStore`; сам back stack переживает поворот экрана и гибель процесса.
  Зависит только от `:domain` — не видит `:data` и Room/Ktor вообще.
- **`:di`** — Koin-модуль фичи: подключает `tasksDataModule`, регистрирует use case'ы и
  ViewModel. Тест графа разрешает публичные точки входа до запуска приложения.

`feature:history` получает выполненные задачи через контракт `feature:tasks:api`
(`CompletedTasksSource`) и маппит их в свою модель `TaskHistoryEntry` — фичи не видят
таблицы и domain-модели друг друга.

Экспериментальные API Material 3 (`TopAppBar`, `PullToRefreshBox`) используются только
внутри `design-system` (`AppTopBar`, `RefreshableContent`), поэтому изменение их API
затронет один модуль, а не каждую фичу.

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

Параметры проекта по [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md): профиль KMP, общий
Compose Multiplatform на iOS, малый размер (две фичи), офлайн-изменения не нужны (P5).

Задачи — эпизоды из `GET https://rickandmortyapi.com/api/episode` (все страницы).
Работа с данными — offline-first:

- экраны читают задачи только из Room; `refresh()` загружает эпизоды и **заменяет кэш
  одной транзакцией**, поэтому записи, удалённые на сервере, не остаются в базе;
- **стратегия изменений (DATA-4) — «сначала хранилище»**: отметка «просмотрено» пишется
  в отдельную таблицу `task_completion` и не затрагивается заменой кэша. Очереди
  синхронизации нет: Rick and Morty API только на чтение, отправлять изменения некуда.
  Если появится бэкенд с записью, отметки отправляются через очередь синхронизации;
- ошибки сети и HTTP-коды переводит в `AppError` `core:network` (`networkResultOf`),
  неожиданные исключения становятся `AppError.Unknown` и логируются через `Logger`.

Схема базы экспортируется в `feature/tasks/data/schemas`. Миграция 1 → 2 (переход с общей
`AppDatabase` с демо-задачами) покрыта тестом `TasksMigrationTest`.

## Тесты

Общие тесты лежат в `commonTest` и запускаются на JVM (Linux-раннер CI) и на
iOS-симуляторе; тесты с настоящей Room в памяти и миграции — в `jvmTest` модуля
`feature:tasks:data`. Fake живут в одном месте: `core:testing` и `feature:tasks:testing`.

- `core`: `AppResultTest`, `DispatcherProviderTest`; `core:network`: `NetworkResultTest` (`MockEngine`)
- `feature/tasks`: `TaskUseCasesTest`, `TaskRepositoryImplTest`, `TaskMappersTest`,
  `TasksDatabaseTest`, `TasksMigrationTest`, `TasksViewModelTest`, `TasksFeatureModuleTest` (граф Koin)
- `feature/history`: `GetTaskHistoryUseCaseTest`, `TaskHistoryRepositoryImplTest`,
  `HistoryViewModelTest`, `HistoryFeatureModuleTest` (граф Koin)

```bash
./gradlew jvmTest
```

```bash
./gradlew iosSimulatorArm64Test
```

## Сборка

```bash
./gradlew :androidApp:assembleDebug
```

Стиль и статический анализ (то же запускает CI):

```bash
./gradlew ktlintCheck detektAll
```

iOS-приложение собирается и запускается через Xcode-проект в [iosApp](iosApp).

## Риски обновления

- **detekt 2.0.0-alpha.6** — предрелизная версия: стабильная 2.0 ещё не вышла
  (см. раздел 15 [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md)). Используется только при сборке,
  в код приложения не попадает. При обновлении проверить правила и `config/detekt/detekt.yml`.
- **Экспериментальные API Material 3** (`TopAppBar`, `PullToRefreshBox`) спрятаны в
  `design-system`; при обновлении Material 3 правки нужны только там.

Осознанные отступления от архитектурного документа записаны в
[docs/deviations.md](docs/deviations.md).
