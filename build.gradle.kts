plugins {
    // Плагины объявляются здесь, чтобы они не загружались повторно в classloader'е каждого подпроекта.
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.ktlint) apply false
    alias(libs.plugins.detekt) apply false
}

val ktlintVersion = libs.versions.ktlint.asProvider().get()

subprojects {
    apply(plugin = "dev.detekt")
    extensions.configure<dev.detekt.gradle.extensions.DetektExtension> {
        buildUponDefaultConfig.set(true)
        config.setFrom(rootProject.file("config/detekt/detekt.yml"))
        parallel.set(true)
    }
    // Сгенерированный код (Room KSP, Compose Resources) не анализируется.
    tasks.withType<dev.detekt.gradle.Detekt>().configureEach {
        exclude { it.file.invariantSeparatorsPath.contains("/build/") }
    }
    // В KMP-модулях detekt создаёт задачу на каждый source set; detektAll запускает их все.
    tasks.register("detektAll") {
        group = "verification"
        description = "Runs detekt for every source set of the module."
        dependsOn(tasks.withType<dev.detekt.gradle.Detekt>().matching { it.name.endsWith("SourceSet") || it.name == "detekt" })
    }

    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    extensions.configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
        // Версия форматтера задаётся только в каталоге версий (BLD-6).
        version.set(ktlintVersion)
        // Сгенерированный код (Compose Resources Res.kt и т.п.) не наш стиль — не линтим его.
        filter {
            exclude("**/build/**")
        }
    }
}

// Проверка границ модулей на этапе конфигурации: любая сборка падает на запрещённой
// зависимости, а не полагается на ревью (правила MOD-1…MOD-5, MOD-9 docs/ARCHITECTURE.md).
// Проверяются объявленные зависимости основного кода; тестовые конфигурации не проверяются.
//  - core и design-system не знают о фичах и о shared; core (common) не зависит от других core;
//  - модули testing подключаются только в тесты;
//  - di фичи подключает только shared; другая фича видна только через её api;
//  - api и domain зависят только от core и (domain) от api своей фичи; их внешние библиотеки —
//    только Kotlin и корутины: ни UI, ни хранилища, ни сети, ни DI, ни сериализации;
//  - presentation и data не видят друг друга; presentation не видит хранилище и сеть
//    (ни core:database / core:network, ни Room, SQLite, Ktor).
gradle.projectsEvaluated {
    val featureLayer = Regex("^:feature:([^:]+):(api|domain|data|presentation|di|testing)$")
    val declaredConfiguration = Regex("^.*(Implementation|Api|CompileOnly|RuntimeOnly)$|^(implementation|api|compileOnly|runtimeOnly)$")
    val pureKotlinLibraries = listOf("org.jetbrains.kotlin:", "org.jetbrains.kotlinx:kotlinx-coroutines-")
    val storageAndNetworkLibraries = listOf("androidx.room", "androidx.sqlite:", "io.ktor:")

    fun isCore(path: String) = path == ":core" || path.startsWith(":core:")

    fun isTesting(path: String) = path == ":core:testing" || featureLayer.matchEntire(path)?.groupValues?.get(2) == "testing"

    val violations = mutableListOf<String>()
    subprojects.forEach { module ->
        val from = module.path
        val fromFeature = featureLayer.matchEntire(from)
        val fromLayer = fromFeature?.groupValues?.get(2)
        val configurations =
            module.configurations.filter {
                declaredConfiguration.matches(it.name) && !it.name.contains("test", ignoreCase = true)
            }

        configurations
            .flatMap { it.dependencies.withType<ProjectDependency>() }
            .map { it.path }
            .distinct()
            .forEach { to ->
                val toFeature = featureLayer.matchEntire(to)
                val toLayer = toFeature?.groupValues?.get(2)
                val sameFeature = toFeature != null && fromFeature?.groupValues?.get(1) == toFeature.groupValues[1]
                val reason =
                    when {
                        (isCore(from) || from == ":design-system") && (toFeature != null || to == ":shared") ->
                            "core and design-system must not know about features or shared"
                        from == ":core" && isCore(to) -> "core (common) must not depend on other core modules"
                        isTesting(to) && !isTesting(from) -> "testing modules may be used only by tests"
                        toLayer == "di" && from != ":shared" -> "only shared may depend on a feature's di"
                        fromFeature == null -> null
                        toFeature != null && !sameFeature && toLayer != "api" -> "another feature is visible only through its api"
                        fromLayer == "api" && to != ":core" -> "api may depend only on core"
                        fromLayer == "domain" && to != ":core" && !(sameFeature && toLayer == "api") ->
                            "domain may depend only on core and its feature's api"
                        fromLayer == "presentation" && to in setOf(":core:network", ":core:database") ->
                            "presentation must not depend on storage or network"
                        fromLayer == "presentation" && sameFeature && toLayer != "domain" -> "presentation may depend only on its domain"
                        fromLayer == "data" && sameFeature && toLayer !in setOf("domain", "api") -> "data must not depend on $toLayer"
                        fromLayer == "testing" && sameFeature && toLayer !in setOf("domain", "api") -> "testing must not depend on $toLayer"
                        else -> null
                    }
                if (reason != null) violations += "$from -> $to: $reason"
            }

        configurations
            .flatMap { it.dependencies.withType<ExternalModuleDependency>() }
            .map { "${it.group}:${it.name}" }
            .distinct()
            .forEach { library ->
                val reason =
                    when (fromLayer) {
                        "api", "domain" ->
                            if (pureKotlinLibraries.none(library::startsWith)) "$fromLayer may use only Kotlin and coroutines" else null
                        "presentation" ->
                            if (storageAndNetworkLibraries.any(library::startsWith)) "presentation must not use storage or network" else null
                        else -> null
                    }
                if (reason != null) violations += "$from -> $library: $reason"
            }
    }
    if (violations.isNotEmpty()) {
        throw GradleException("Нарушены границы модулей:\n" + violations.joinToString("\n") { "  - $it" })
    }
}

// Генератор скелета новой фичи: domain/data/presentation/di как отдельные Gradle-модули,
// по образцу feature/tasks и feature/history.
// Пример: ./gradlew newFeature -PfeatureName=reminders
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
                            implementation(project(":core:testing"))
                        }
                    }
                }
            """.trimIndent() + "\n",
            readme = """
                # $gradlePath:domain

                Модели, интерфейс репозитория, use case'ы. Никакого Android/Compose/Room/Ktor —
                только `core` и `kotlinx.coroutines` (проверяется сборкой). Use case заводится,
                только если в нём есть логика (DOM-4). Тесты — в `commonTest` через `runTest`.
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
                            implementation(libs.koin.core)
                            implementation(libs.kotlinx.coroutines.core)
                            // TODO: сеть — implementation(project(":core:network")), вызовы через networkResultOf;
                            // своя база — implementation(project(":core:database")) и плагины ksp/room3
                            // по образцу feature/tasks/data.
                        }
                        commonTest.dependencies {
                            implementation(project(":core:testing"))
                        }
                    }
                }
            """.trimIndent() + "\n",
            readme = """
                # $gradlePath:data

                Реализация интерфейса репозитория из `:domain`. Реализации, Entity, DAO и DTO
                объявляются `internal`; наружу модуль отдаёт только Koin-модуль `<name>DataModule`,
                который подключает `:di`. Мапперы лежат в отдельном файле и покрыты тестами.
            """.trimIndent() + "\n",
        )

        module(
            layer = "presentation",
            buildFile = """
                plugins {
                    id("kmpcleanarchsample.kmp.compose")
                    alias(libs.plugins.kotlinSerialization)
                }

                kotlin {
                    sourceSets {
                        commonMain.dependencies {
                            implementation(project(":core"))
                            implementation(project("$gradlePath:domain"))
                            implementation(project(":design-system"))
                            implementation(libs.koin.core)
                            implementation(libs.kotlinx.coroutines.core)
                            implementation(libs.kotlinx.serialization.core)
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

                `ViewModel` с `uiState: StateFlow<UiState>` (через `stateIn`) и методами `onXxx`,
                пара `XxxRoute`/`XxxScreen(uiState, onXxx, modifier)` с превью каждого состояния,
                `@Serializable`-ключи `NavKey` и `NavDisplay` вкладки на `rememberNavBackStack`
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

                Koin-модуль фичи: подключает `<name>DataModule` из `:data`, регистрирует use case'ы
                и ViewModel (`viewModelOf`) из `:presentation`. Тест графа в `commonTest`
                разрешает репозиторий и ViewModel (DI-3). Подключается в `shared`/`Koin.kt`.
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
              2. Реализовать репозиторий в data (internal) и Koin-модуль <name>DataModule; см. TODO в data/build.gradle.kts.
              3. Написать ViewModel, Route/Screen и NavDisplay вкладки в presentation (образец — feature/tasks).
              4. Собрать Koin-модуль в di (includes(<name>DataModule)), написать тест графа и подключить его в shared/Koin.kt.
              5. Добавить в shared/build.gradle.kts:
                   implementation(project("$gradlePath:presentation"))
                   implementation(project("$gradlePath:di"))
              6. Добавить вкладку в AppTab в shared/App.kt (если нужна отдельная вкладка).
            """.trimIndent()
        )
    }
}