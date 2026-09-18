#!/usr/bin/env bash
#
# Переименовывает базовый Kotlin-пакет и имя проекта при использовании этого
# репозитория как шаблона для нового проекта.
#
# Usage:
#   scripts/rename-template.sh [<new-package> [<new-app-name>]] [--dry-run]
#
# Пример:
#   scripts/rename-template.sh com.acme.myapp AcmeApp
#
# Без аргументов — интерактивные вопросы. --dry-run — только показать, что
# изменится, ничего не трогая.
#
# Что делает:
#   1. Заменяет "ru.marwinka.kmpcleanarchsample" -> <new-package> и
#      "KMPCleanArchSample" -> <new-app-name> во всех git-tracked текстовых
#      файлах (package/import в .kt, gradle.properties, Config.xcconfig,
#      settings.gradle.kts, strings.xml, README и т.п.).
#   2. Физически переносит директории src/**/kotlin/ru/marwinka/kmpcleanarchsample
#      (во всех модулях и в build-logic) на путь нового пакета через git mv.
#
# Что НЕ делает (сознательно):
#   - Не трогает build-logic/convention plugin ID (kmpcleanarchsample.kmp.library/
#     .kmp.compose) — это внутренняя механика Gradle, наружу не влияет.
#   - Не трогает TEAM_ID/подпись в iosApp/Configuration/Config.xcconfig.
#   - Не переименовывает сам .xcodeproj/.xcworkspace (только PRODUCT_NAME/
#     PRODUCT_BUNDLE_IDENTIFIER внутри Config.xcconfig).

set -euo pipefail

SCRIPT_PATH="scripts/rename-template.sh"
DRY_RUN=false
POSITIONAL=()

for arg in "$@"; do
    case "$arg" in
        --dry-run) DRY_RUN=true ;;
        -h|--help)
            sed -n '2,25p' "$0" | sed 's/^# \{0,1\}//'
            exit 0
            ;;
        *) POSITIONAL+=("$arg") ;;
    esac
done

NEW_PACKAGE="${POSITIONAL[0]:-}"
NEW_APP_NAME="${POSITIONAL[1]:-}"

cd "$(git rev-parse --show-toplevel)"

if [[ -n "$(git status --porcelain)" ]]; then
    echo "error: рабочее дерево не чистое (git status показывает изменения)." >&2
    echo "Закоммитьте или отложите изменения перед переименованием — скрипт делает" >&2
    echo "массовые правки и git mv, откатывать их вручную неприятно." >&2
    exit 1
fi

OLD_PACKAGE=$(grep '^rootPackage=' gradle.properties | cut -d= -f2)
OLD_APP_NAME=$(grep 'rootProject.name' settings.gradle.kts | sed -E 's/.*"(.*)".*/\1/')

if [[ -z "$NEW_PACKAGE" ]]; then
    read -r -p "Новый базовый пакет [текущий: $OLD_PACKAGE]: " NEW_PACKAGE
fi
if [[ -z "$NEW_APP_NAME" ]]; then
    read -r -p "Новое имя проекта [текущее: $OLD_APP_NAME]: " NEW_APP_NAME
fi

if [[ ! "$NEW_PACKAGE" =~ ^[a-z][a-z0-9]*(\.[a-z][a-z0-9]*)+$ ]]; then
    echo "error: пакет должен быть в формате 'com.example.app' (lowercase, через точку), получено: '$NEW_PACKAGE'" >&2
    exit 1
fi
TRIMMED_APP_NAME="$(echo -n "$NEW_APP_NAME" | xargs)"
if [[ -z "$TRIMMED_APP_NAME" ]]; then
    echo "error: имя проекта не может быть пустым" >&2
    exit 1
fi
NEW_APP_NAME="$TRIMMED_APP_NAME"

echo "Пакет:  $OLD_PACKAGE -> $NEW_PACKAGE"
echo "Имя:    $OLD_APP_NAME -> $NEW_APP_NAME"
$DRY_RUN && echo "(dry-run — изменения не записываются)"
echo

replace_string() {
    local old="$1" new="$2"
    local files
    files=$(git grep -lI -F -- "$old" | grep -v -F "$SCRIPT_PATH" || true)

    if [[ -z "$files" ]]; then
        echo "  '$old' -> '$new': совпадений не найдено"
        return
    fi

    local count
    count=$(echo "$files" | wc -l | xargs)
    echo "  '$old' -> '$new': $count файл(ов)"

    if $DRY_RUN; then
        echo "$files" | sed 's/^/    /'
        return
    fi

    while IFS= read -r file; do
        OLD="$old" NEW="$new" perl -pi -e 's/\Q$ENV{OLD}\E/$ENV{NEW}/g' -- "$file"
    done <<< "$files"
}

echo "Текстовая замена:"
replace_string "$OLD_PACKAGE" "$NEW_PACKAGE"
replace_string "$OLD_APP_NAME" "$NEW_APP_NAME"
echo

echo "Перенос директорий (package -> path):"
OLD_PACKAGE_PATH=$(echo "$OLD_PACKAGE" | tr '.' '/')
NEW_PACKAGE_PATH=$(echo "$NEW_PACKAGE" | tr '.' '/')

DIRS=$(find . -not -path './.git/*' -not -path '*/build/*' -not -path '*/.gradle/*' -not -path '*/.kotlin/*' \
    -type d -path "*/$OLD_PACKAGE_PATH" || true)

if [[ -z "$DIRS" ]]; then
    echo "  директорий, зеркалящих '$OLD_PACKAGE_PATH', не найдено"
else
    while IFS= read -r dir; do
        dest="${dir%$OLD_PACKAGE_PATH}$NEW_PACKAGE_PATH"
        echo "  $dir -> $dest"
        if ! $DRY_RUN; then
            mkdir -p "$(dirname "$dest")"
            git mv "$dir" "$dest"
        fi
    done <<< "$DIRS"
fi
echo

if $DRY_RUN; then
    echo "Dry-run завершён, ничего не изменено."
    exit 0
fi

find . -not -path './.git/*' -not -path '*/build/*' -not -path '*/.gradle/*' -not -path '*/.kotlin/*' \
    -depth -type d -empty -delete

# Смена пакета почти всегда меняет алфавитный порядок импортов (например,
# "com.acme..." теперь может идти раньше "org.koin..."), из-за чего ktlintCheck
# упадёт на чисто механическом поводе. Прогоняем autoformat сразу, чтобы
# результат уже был чистым, а не оставлял разработчику разбираться самому.
echo "Автоформатирование (ktlintFormat) — правит порядок импортов после смены пакета..."
# --rerun-tasks: сразу после массового git mv/правки инкрементальная проверка
# ktlint-gradle иногда считает таск актуальным и молча пропускает файлы,
# которые физически только что переехали/изменились — форсируем честный прогон.
if ! ./gradlew ktlintFormat --console=plain --rerun-tasks; then
    echo "warning: ktlintFormat завершился с ошибкой — проверьте вручную (./gradlew ktlintCheck)." >&2
fi
echo

echo "Готово. Дальше:"
echo "  1. ./gradlew ktlintCheck allTests :androidApp:assembleDebug"
echo "  2. xcodebuild build -project iosApp/iosApp.xcodeproj -scheme iosApp \\"
echo "       -destination 'generic/platform=iOS Simulator' CODE_SIGNING_ALLOWED=NO"
echo "  3. Проверить iosApp/Configuration/Config.xcconfig — TEAM_ID специально не тронут."
echo "  4. git status / git diff — просмотреть, затем закоммитить."
