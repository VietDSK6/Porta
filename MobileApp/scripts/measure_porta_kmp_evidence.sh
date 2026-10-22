#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
MOBILE_APP_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
REPOSITORY_ROOT="$(git -C "$MOBILE_APP_DIR" rev-parse --show-toplevel)"

BASELINE_REF="${1:-bd5fd5d}"
HEAD_REF="${2:-HEAD}"

git -C "$REPOSITORY_ROOT" cat-file -e "${BASELINE_REF}^{commit}"
git -C "$REPOSITORY_ROOT" cat-file -e "${HEAD_REF}^{commit}"

WORK_DIR="$(mktemp -d)"
trap 'rm -rf "$WORK_DIR"' EXIT

ADDED_FILES="$WORK_DIR/added-kotlin.txt"
SHARED_PRODUCTION_FILES="$WORK_DIR/shared-production.txt"
PLATFORM_PRODUCTION_FILES="$WORK_DIR/platform-production.txt"
KOTLIN_DATA_FILES="$WORK_DIR/kotlin-data.txt"
SHARED_TEST_FILES="$WORK_DIR/shared-tests.txt"
PLATFORM_TEST_FILES="$WORK_DIR/platform-tests.txt"
EXCLUDED_FILES="$WORK_DIR/excluded.txt"

touch \
    "$SHARED_PRODUCTION_FILES" \
    "$PLATFORM_PRODUCTION_FILES" \
    "$KOTLIN_DATA_FILES" \
    "$SHARED_TEST_FILES" \
    "$PLATFORM_TEST_FILES" \
    "$EXCLUDED_FILES"

git -C "$REPOSITORY_ROOT" diff \
    --name-status \
    --find-renames=50% \
    "${BASELINE_REF}..${HEAD_REF}" \
    -- '*.kt' |
    awk '$1 == "A" { print $2 }' |
    sort > "$ADDED_FILES"

SHARED_PRODUCTION_EXACT_PATHS=(
    "MobileApp/shared/src/commonMain/kotlin/io/github/vietdsk6/porta/data/repository/OpportunityPackRepository.kt"
    "MobileApp/shared/src/commonMain/kotlin/io/github/vietdsk6/porta/data/repository/PlanRepository.kt"
    "MobileApp/shared/src/commonMain/kotlin/io/github/vietdsk6/porta/data/repository/ProfileRepository.kt"
    "MobileApp/shared/src/commonMain/kotlin/io/github/vietdsk6/porta/data/source/local/dao/OpportunityPackDao.kt"
    "MobileApp/shared/src/commonMain/kotlin/io/github/vietdsk6/porta/data/source/local/dao/PlanItemDao.kt"
    "MobileApp/shared/src/commonMain/kotlin/io/github/vietdsk6/porta/data/source/local/dao/ProfileFactDao.kt"
    "MobileApp/shared/src/commonMain/kotlin/io/github/vietdsk6/porta/data/source/local/dao/SavedOpportunityDao.kt"
    "MobileApp/shared/src/commonMain/kotlin/io/github/vietdsk6/porta/data/source/local/entity/FactValueStorage.kt"
    "MobileApp/shared/src/commonMain/kotlin/io/github/vietdsk6/porta/data/source/local/entity/OpportunityPackEntity.kt"
    "MobileApp/shared/src/commonMain/kotlin/io/github/vietdsk6/porta/data/source/local/entity/PlanItemEntity.kt"
    "MobileApp/shared/src/commonMain/kotlin/io/github/vietdsk6/porta/data/source/local/entity/ProfileFactEntity.kt"
    "MobileApp/shared/src/commonMain/kotlin/io/github/vietdsk6/porta/data/source/local/entity/SavedOpportunityEntity.kt"
    "MobileApp/shared/src/commonMain/kotlin/io/github/vietdsk6/porta/data/source/local/pack/BundledOpportunityPackProvider.kt"
    "MobileApp/shared/src/commonMain/kotlin/io/github/vietdsk6/porta/data/source/local/pack/OpportunityPackCodec.kt"
    "MobileApp/shared/src/commonMain/kotlin/io/github/vietdsk6/porta/data/source/local/pack/OpportunityPackDto.kt"
    "MobileApp/shared/src/commonMain/kotlin/io/github/vietdsk6/porta/data/source/local/pack/OpportunityPackParser.kt"
    "MobileApp/shared/src/commonMain/kotlin/io/github/vietdsk6/porta/data/source/local/pack/OpportunityPackValidator.kt"
    "MobileApp/shared/src/commonMain/kotlin/io/github/vietdsk6/porta/domain/model/ActionFrontier.kt"
    "MobileApp/shared/src/commonMain/kotlin/io/github/vietdsk6/porta/domain/model/Opportunity.kt"
    "MobileApp/shared/src/commonMain/kotlin/io/github/vietdsk6/porta/domain/model/OpportunityPack.kt"
    "MobileApp/shared/src/commonMain/kotlin/io/github/vietdsk6/porta/domain/model/OpportunityPackDocument.kt"
    "MobileApp/shared/src/commonMain/kotlin/io/github/vietdsk6/porta/domain/model/OpportunityPackImport.kt"
    "MobileApp/shared/src/commonMain/kotlin/io/github/vietdsk6/porta/domain/model/PlanItem.kt"
    "MobileApp/shared/src/commonMain/kotlin/io/github/vietdsk6/porta/domain/model/ProfileFact.kt"
    "MobileApp/shared/src/commonMain/kotlin/io/github/vietdsk6/porta/domain/model/SavedOpportunity.kt"
    "MobileApp/shared/src/commonMain/kotlin/io/github/vietdsk6/porta/domain/usecase/ActionFrontierEngine.kt"
    "MobileApp/shared/src/commonMain/kotlin/io/github/vietdsk6/porta/domain/usecase/EligibilityEvaluator.kt"
    "MobileApp/shared/src/commonMain/kotlin/io/github/vietdsk6/porta/domain/usecase/ScenarioEvaluator.kt"
    "MobileApp/shared/src/commonMain/kotlin/io/github/vietdsk6/porta/presentation/navigation/AdaptiveAppScaffold.kt"
)

SHARED_PRODUCTION_PREFIXES=(
    "MobileApp/shared/src/commonMain/kotlin/io/github/vietdsk6/porta/data/source/local/profile/"
    "MobileApp/shared/src/commonMain/kotlin/io/github/vietdsk6/porta/presentation/screens/activeplan/"
    "MobileApp/shared/src/commonMain/kotlin/io/github/vietdsk6/porta/presentation/screens/actualprofileupdate/"
    "MobileApp/shared/src/commonMain/kotlin/io/github/vietdsk6/porta/presentation/screens/doors/"
    "MobileApp/shared/src/commonMain/kotlin/io/github/vietdsk6/porta/presentation/screens/frontier/"
    "MobileApp/shared/src/commonMain/kotlin/io/github/vietdsk6/porta/presentation/screens/opportunitydetail/"
    "MobileApp/shared/src/commonMain/kotlin/io/github/vietdsk6/porta/presentation/screens/packinspector/"
    "MobileApp/shared/src/commonMain/kotlin/io/github/vietdsk6/porta/presentation/screens/profilesetup/"
    "MobileApp/shared/src/commonMain/kotlin/io/github/vietdsk6/porta/presentation/screens/scenariopreview/"
    "MobileApp/shared/src/commonMain/kotlin/io/github/vietdsk6/porta/presentation/screens/studentprofile/"
)

is_shared_production_file() {
    local source_path="$1"
    local candidate

    for candidate in "${SHARED_PRODUCTION_EXACT_PATHS[@]}"; do
        [[ "$source_path" == "$candidate" ]] && return 0
    done
    for candidate in "${SHARED_PRODUCTION_PREFIXES[@]}"; do
        [[ "$source_path" == "$candidate"* ]] && return 0
    done
    return 1
}

while IFS= read -r source_path; do
    if is_shared_production_file "$source_path"; then
        echo "$source_path" >> "$SHARED_PRODUCTION_FILES"
    elif [[ "$source_path" == MobileApp/androidApp/src/main/kotlin/* ]] ||
        [[ "$source_path" == MobileApp/desktopApp/src/main/kotlin/* ]] ||
        [[ "$source_path" == MobileApp/shared/src/androidMain/kotlin/io/github/vietdsk6/porta/* ]] ||
        [[ "$source_path" == MobileApp/shared/src/jvmMain/kotlin/io/github/vietdsk6/porta/* ]]; then
        echo "$source_path" >> "$PLATFORM_PRODUCTION_FILES"
    elif [[ "$source_path" == MobileApp/shared/src/commonMain/kotlin/io/github/vietdsk6/porta/data/sample/PortaSampleData.kt ]] ||
        [[ "$source_path" == MobileApp/shared/src/commonMain/kotlin/io/github/vietdsk6/porta/data/source/local/pack/PortaContestPackData.kt ]]; then
        echo "$source_path" >> "$KOTLIN_DATA_FILES"
    elif [[ "$source_path" == MobileApp/shared/src/commonTest/kotlin/io/github/vietdsk6/porta/* ]]; then
        echo "$source_path" >> "$SHARED_TEST_FILES"
    elif [[ "$source_path" == MobileApp/shared/src/jvmTest/kotlin/io/github/vietdsk6/porta/* ]] ||
        [[ "$source_path" == MobileApp/shared/src/androidHostTest/kotlin/io/github/vietdsk6/porta/* ]]; then
        echo "$source_path" >> "$PLATFORM_TEST_FILES"
    else
        echo "$source_path" >> "$EXCLUDED_FILES"
    fi
done < "$ADDED_FILES"

count_files() {
    local list_file="$1"
    awk 'END { print NR + 0 }' "$list_file"
}

count_ncloc() {
    local list_file="$1"
    local total=0

    while IFS= read -r source_path; do
        local source_file="$REPOSITORY_ROOT/$source_path"
        local source_lines
        source_lines="$(
            awk '
                {
                    line = $0
                    gsub(/^[[:space:]]+|[[:space:]]+$/, "", line)
                    if (line == "" || line ~ /^\/\// || line ~ /^\/\*/ || line ~ /^\*/ || line ~ /^\*\//) {
                        next
                    }
                    count++
                }
                END { print count + 0 }
            ' "$source_file"
        )"
        total=$((total + source_lines))
    done < "$list_file"

    echo "$total"
}

SHARED_PRODUCTION_COUNT="$(count_files "$SHARED_PRODUCTION_FILES")"
PLATFORM_PRODUCTION_COUNT="$(count_files "$PLATFORM_PRODUCTION_FILES")"
KOTLIN_DATA_COUNT="$(count_files "$KOTLIN_DATA_FILES")"
SHARED_TEST_COUNT="$(count_files "$SHARED_TEST_FILES")"
PLATFORM_TEST_COUNT="$(count_files "$PLATFORM_TEST_FILES")"
EXCLUDED_COUNT="$(count_files "$EXCLUDED_FILES")"

SHARED_PRODUCTION_NCLOC="$(count_ncloc "$SHARED_PRODUCTION_FILES")"
PLATFORM_PRODUCTION_NCLOC="$(count_ncloc "$PLATFORM_PRODUCTION_FILES")"
KOTLIN_DATA_NCLOC="$(count_ncloc "$KOTLIN_DATA_FILES")"
SHARED_TEST_NCLOC="$(count_ncloc "$SHARED_TEST_FILES")"
PLATFORM_TEST_NCLOC="$(count_ncloc "$PLATFORM_TEST_FILES")"

PRODUCTION_NCLOC=$((SHARED_PRODUCTION_NCLOC + PLATFORM_PRODUCTION_NCLOC))
if ((PRODUCTION_NCLOC == 0)); then
    SHARED_PERCENT="n/a"
else
    SHARED_PERCENT="$(
        awk -v shared="$SHARED_PRODUCTION_NCLOC" -v total="$PRODUCTION_NCLOC" \
            'BEGIN { printf "%.1f", (shared / total) * 100 }'
    )"
fi

printf 'Porta KMP evidence\n'
printf 'Baseline: %s (%s)\n' "$BASELINE_REF" "$(git -C "$REPOSITORY_ROOT" log -1 --format=%s "$BASELINE_REF")"
printf 'Measured head: %s\n' "$(git -C "$REPOSITORY_ROOT" rev-parse --short "$HEAD_REF")"
printf '\n'
printf '%-30s %8s %10s\n' 'Category' 'Files' 'NCLOC'
printf '%-30s %8s %10s\n' 'Shared production' "$SHARED_PRODUCTION_COUNT" "$SHARED_PRODUCTION_NCLOC"
printf '%-30s %8s %10s\n' 'Platform production' "$PLATFORM_PRODUCTION_COUNT" "$PLATFORM_PRODUCTION_NCLOC"
printf '%-30s %8s %10s\n' 'Kotlin data/fixtures' "$KOTLIN_DATA_COUNT" "$KOTLIN_DATA_NCLOC"
printf '%-30s %8s %10s\n' 'Shared tests' "$SHARED_TEST_COUNT" "$SHARED_TEST_NCLOC"
printf '%-30s %8s %10s\n' 'Platform tests' "$PLATFORM_TEST_COUNT" "$PLATFORM_TEST_NCLOC"
printf '%-30s %8s %10s\n' 'Excluded added Kotlin files' "$EXCLUDED_COUNT" '-'
printf '\n'
printf 'Shared production ratio: %s%%\n' "$SHARED_PERCENT"
printf 'Definition: nonblank, non-comment-only lines in newly added, explicitly allowlisted Porta Kotlin files.\n'
