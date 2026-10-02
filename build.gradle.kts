plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    // RU: это нужно, чтобы плагины не загружались повторно в classloader'е каждого подпроекта
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.ktlint) apply false
}

subprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    extensions.configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
        // Сгенерированный код (Compose Resources Res.kt и т.п.) не наш стиль — не линтим его.
        filter {
            exclude("**/build/**")
        }
    }
}

// Проверка границ модулей на этапе конфигурации: любая сборка падает на запрещённой
// зависимости между проектами, а не полагается на ревью. Правила — из архитектурного документа:
//  - core, design-system и общие модули хранения data:* (если появятся) не знают о фичах и о shared;
//  - api фичи не зависит ни от каких модулей фич; domain — только от api своей фичи;
//  - presentation и domain не видят data (ни свой, ни data:*); di не видит никто, кроме shared;
//  - другая фича видна только через её api.
// Тестовые конфигурации не проверяются.
gradle.projectsEvaluated {
    val featureLayer = Regex("^:feature:([^:]+):(api|domain|data|presentation|di)$")
    val violations = mutableListOf<String>()
    subprojects.forEach { module ->
        val from = module.path
        val fromFeature = featureLayer.matchEntire(from)
        val fromInfrastructure = from == ":core" || from.startsWith(":core:") || from == ":design-system" || from.startsWith(":data:")
        module.configurations
            .filter { !it.name.contains("test", ignoreCase = true) }
            .flatMap { it.dependencies.withType<ProjectDependency>() }
            .map { it.path }
            .distinct()
            .forEach { to ->
                val toFeature = featureLayer.matchEntire(to)
                val toLayer = toFeature?.groupValues?.get(2)
                val reason =
                    when {
                        fromInfrastructure && (toFeature != null || to == ":shared") ->
                            "infrastructure modules must not know about features or shared"
                        fromFeature == null -> null
                        toLayer == "di" && fromFeature.groupValues[2] != "di" -> "only shared may depend on a feature's di"
                        fromFeature.groupValues[2] in setOf("domain", "presentation", "api") && to.startsWith(":data:") ->
                            "${fromFeature.groupValues[2]} must not depend on data"
                        toFeature == null -> null
                        fromFeature.groupValues[1] != toFeature.groupValues[1] ->
                            if (toLayer == "api") null else "another feature is visible only through its api"
                        fromFeature.groupValues[2] == "api" -> "api must not depend on feature modules"
                        fromFeature.groupValues[2] == "domain" && toLayer != "api" -> "domain may depend only on its feature's api"
                        fromFeature.groupValues[2] in setOf("presentation", "data") && toLayer in setOf("data", "presentation") ->
                            "${fromFeature.groupValues[2]} must not depend on $toLayer"
                        else -> null
                    }
                if (reason != null) violations += "$from -> $to: $reason"
            }
    }
    if (violations.isNotEmpty()) {
        throw GradleException("Нарушены границы модулей:\n" + violations.joinToString("\n") { "  - $it" })
    }
}

// Генератор скелета новой фичи: domain/data/presentation/di как отдельные Gradle-модули,
// по образцу feature/tasks и feature/history.
// RU: ./gradlew newFeature -PfeatureName=reminders
tasks.register("newFeature") {
    group = "template"
    description = "Scaffolds a new feature module set (domain/data/presentation/di). Usage: -PfeatureName=<name>"

    // Значения проекта захватываются на этапе конфигурации — при configuration cache
    // обращаться к Task.project/rootDir в doLast нельзя.
    val featureNameProvider = providers.gradleProperty("featureName")
    val rootPackageProvider = providers.gradleProperty("rootPackage")
    val rootDirectory = rootDir

    doLast {
        val featureName = featureNameProvider.orNull?.trim()
            ?: error("Missing -PfeatureName=<name>. Example: ./gradlew newFeature -PfeatureName=reminders")

        if (!featureName.matches(Regex("^[a-z][a-zA-Z0-9]*$"))) {
            error("featureName must start with a lowercase letter and contain only letters/digits, got '$featureName'")
        }

        val featureRoot = java.io.File(rootDirectory, "feature/$featureName")
        if (featureRoot.exists()) {
            error("feature/$featureName already exists")
        }

        val packagePath = "${rootPackageProvider.get().replace('.', '/')}/feature/$featureName"
        val gradlePath = ":feature:$featureName"

        fun sourceDir(layer: String, sourceSet: String) =
            java.io.File(featureRoot, "$layer/src/$sourceSet/kotlin/$packagePath/$layer").apply { mkdirs() }

        fun module(layer: String, buildFile: String, readme: String) {
            sourceDir(layer, "commonMain").resolve(".gitkeep").writeText("")
            java.io.File(featureRoot, "$layer/build.gradle.kts").writeText(buildFile)
            java.io.File(featureRoot, "$layer/README.md").writeText(readme)
        }

        module(
            layer = "domain",
            buildFile = """
                plugins {
                    id("kmpcleanarchsample.kmp.library")
                }

                kotlin {
                    sourceSets {
                        commonMain.dependencies {
                            implementation(project(":core"))
                            implementation(libs.kotlinx.coroutines.core)
                        }
                        commonTest.dependencies {
                            implementation(libs.kotlinx.coroutines.core)
                        }
                    }
                }
            """.trimIndent() + "\n",
            readme = """
                # $gradlePath:domain

                Модели, интерфейс репозитория, use case'ы. Никакого Android/Compose/Room/Ktor —
                только `core` и `kotlinx.coroutines`. Тесты use case'ов (с рукописными фейками
                репозитория) кладутся сюда же, в `commonTest`.
            """.trimIndent() + "\n",
        )
        sourceDir("domain", "commonTest").resolve(".gitkeep").writeText("")

        module(
            layer = "data",
            buildFile = """
                plugins {
                    id("kmpcleanarchsample.kmp.library")
                }

                kotlin {
                    sourceSets {
                        commonMain.dependencies {
                            implementation(project(":core"))
                            implementation(project("$gradlePath:domain"))
                            // TODO: Room-сущности и DAO фичи — здесь (api(libs.androidx.room3.runtime)), сущность добавить в AppDatabase в :shared;
                            // сеть — через HttpClient из :core:network.
                            // api (не implementation), если Entity/Dao из data-модуля входят в публичный
                            // конструктор *RepositoryImpl — на него ссылается $gradlePath:di.
                            implementation(libs.kotlinx.coroutines.core)
                        }
                    }
                }
            """.trimIndent() + "\n",
            readme = """
                # $gradlePath:data

                Реализация интерфейса репозитория из `:domain`. Класс `*RepositoryImpl` должен
                быть публичным (не `internal`) — на него по имени ссылается `:di`, а Kotlin
                `internal` не пересекает границы Gradle-модулей. Мапит Entity/DTO источника
                данных в domain-модель.
            """.trimIndent() + "\n",
        )

        module(
            layer = "presentation",
            buildFile = """
                plugins {
                    id("kmpcleanarchsample.kmp.compose")
                }

                kotlin {
                    sourceSets {
                        commonMain.dependencies {
                            implementation(project(":core"))
                            implementation(project("$gradlePath:domain"))
                            implementation(project(":design-system"))
                            implementation(libs.koin.core)
                            implementation(libs.kotlinx.coroutines.core)
                            implementation(libs.compose.runtime)
                            implementation(libs.compose.foundation)
                            implementation(libs.compose.material3)
                            implementation(libs.compose.ui)
                            implementation(libs.navigation3.ui)
                            implementation(libs.androidx.lifecycle.viewmodelNavigation3)
                            implementation(libs.androidx.lifecycle.runtimeCompose)
                            // api: ViewModel — супертип ViewModel фичи, на который ссылается $gradlePath:di
                            api(libs.androidx.lifecycle.viewmodelCompose)
                            implementation(libs.koin.compose.viewmodel)
                            implementation(libs.compose.uiToolingPreview)
                        }
                    }
                }
            """.trimIndent() + "\n",
            readme = """
                # $gradlePath:presentation

                `ViewModel` + `StateFlow<UiState>` (через `stateIn`), пара `Route`/`Content`
                (stateful/stateless) с `@Preview`, ключи навигации и `NavDisplay` вкладки
                (Navigation 3; ViewModel привязана к записи back stack).
                Зависит только от `:domain` (use case'ы) — НЕ от `:data`. Реализация
                репозитория подставляется через Koin в `:di`.
            """.trimIndent() + "\n",
        )

        module(
            layer = "di",
            buildFile = """
                plugins {
                    id("kmpcleanarchsample.kmp.library")
                }

                kotlin {
                    sourceSets {
                        commonMain.dependencies {
                            implementation(project("$gradlePath:domain"))
                            implementation(project("$gradlePath:data"))
                            implementation(project("$gradlePath:presentation"))
                            implementation(libs.koin.core)
                            implementation(libs.koin.core.viewmodel)
                        }
                    }
                }
            """.trimIndent() + "\n",
            readme = """
                # $gradlePath:di

                Koin-модуль фичи: связывает интерфейс репозитория из `:domain` с реализацией
                из `:data`, регистрирует use case'ы и ViewModel (`viewModelOf`) из `:presentation`.
                Подключается в `shared`/`Koin.kt` вместе с `:presentation` (для вкладки).
            """.trimIndent() + "\n",
        )

        val settingsFile = java.io.File(rootDirectory, "settings.gradle.kts")
        val lines = settingsFile.readLines().toMutableList()
        val insertAt = lines.indexOfLast { it.trim().startsWith("include(\":feature:") } + 1
        val newIncludes = listOf("domain", "data", "presentation", "di")
            .map { layer -> "include(\"$gradlePath:$layer\")" }
        lines.addAll(if (insertAt > 0) insertAt else lines.size, newIncludes)
        settingsFile.writeText(lines.joinToString("\n") + "\n")

        logger.lifecycle(
            """
            Создана фича '$featureName' в feature/$featureName/{domain,data,presentation,di}.
            Модули добавлены в settings.gradle.kts.

            Осталось вручную:
              1. Заполнить domain (модель, интерфейс репозитория, use case'ы).
              2. Реализовать репозиторий в data (см. TODO про источник данных в data/build.gradle.kts).
              3. Написать ViewModel, Route/Content и NavDisplay вкладки в presentation (образец — feature/tasks).
              4. Собрать Koin-модуль в di и подключить его в shared/Koin.kt (modules(...)).
              5. Добавить в shared/build.gradle.kts:
                   implementation(project("$gradlePath:presentation"))
                   implementation(project("$gradlePath:di"))
              6. Добавить вкладку в AppTab в shared/App.kt (если нужна отдельная вкладка).
            """.trimIndent()
        )
    }
}