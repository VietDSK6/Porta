# Porta shared Kotlin evidence

> Snapshot date: 2 November 2026
> Baseline: `bd5fd5d` — the last repository commit before Porta work began in `6be9ec1`
> Measured Kotlin head: `d3d34dc`

This document records an internal, reproducible view of where **new Porta-owned Kotlin** lives. It is not an
official contest formula or a judging score. The Kotlin Student Coding Competition rules do not prescribe a shared
code ratio.

## Reproduce the measurement

Run from `MobileApp/`:

```bash
./scripts/measure_porta_kmp_evidence.sh
```

The script accepts optional baseline and head refs:

```bash
./scripts/measure_porta_kmp_evidence.sh bd5fd5d HEAD
```

Snapshot output:

| Category | Files | NCLOC |
|---|---:|---:|
| Shared production | 75 | 14,933 |
| Platform production | 4 | 96 |
| Kotlin data/fixtures | 2 | 1,075 |
| Shared tests | 16 | 2,658 |
| Platform-run tests | 26 | 4,572 |
| Excluded added Kotlin files | 18 | — |

Within this deliberately narrow production sample, the shared proportion is **14,933 / 15,029 = 99.4%**.

That number means all but 96 NCLOC of newly added, explicitly allowlisted Porta production Kotlin is currently shared.
The four measured platform files provide narrowly scoped target integration. It does **not** mean 99.4% of the whole repository
is shared, nor that platform code is unnecessary. Android and Desktop still use target launchers, drivers, packaging,
and platform services inherited from the starter kit; those inherited lines are excluded rather than claimed as
Porta work.

## Method

1. Start at `bd5fd5d`, the commit immediately before the first Porta commit.
2. Ask Git for Kotlin files added between the baseline and the measured head with rename detection enabled.
3. Include only an explicit Porta allowlist covering:
   - opportunity/profile/plan repositories and Room entities/DAOs;
   - pack parsing, validation, and profile import/export;
   - typed domain models and the eligibility, scenario, and Action Frontier engines;
   - Porta feature screens, state, ViewModels, and the adaptive navigation scaffold.
4. Count nonblank, non-comment-only source lines (NCLOC).
5. Report tests and static Kotlin data separately from production code.

The script intentionally requires allowlist maintenance when a new product area is added. A new file is not silently
treated as contest-owned evidence just because it appears under the Porta package.

## Exclusions

The production ratio excludes:

- generated files and build outputs;
- dependencies, vendored code, Gradle/build logic, and resources;
- unchanged starter-kit code and package-only renames;
- the 15-opportunity catalog and sample profile fixture, reported separately as Kotlin data;
- tests, reported separately;
- inherited Android/Desktop launchers and platform services;
- optional auth, subscription, credit, ads, AI, and media-generation scaffold;
- `AppNavigation.kt`, `Routes.kt`, and `AppDatabase.kt`, despite substantial Porta changes, because they replace or
  integrate with pre-existing starter files;
- onboarding variation files derived from starter-kit screens;
- compatibility subscription stubs and ads interfaces added during package/platform maintenance.

These exclusions make the result conservative. In particular, the ratio does not take credit for shared integration
files when contest-period provenance is mixed with starter code.

## What is meaningfully shared

The stronger evidence is behavioral rather than numerical:

- one typed rule graph and deterministic eligibility evaluator;
- one counterfactual scenario engine and constrained Pareto Action Frontier;
- shared opportunity-pack validation, import/export, Room persistence, repositories, and reactive state;
- shared Compose screens and ViewModels used by compact Android and wide Desktop layouts;
- the same profile and opportunity pack producing the same receipts on both declared targets;
- common/JVM/Android-host tests covering engines, persistence, ViewModels, and adaptive UI.

The Android-to-Desktop handoff is also shared Kotlin rather than a platform-specific duplicate: Android exports the
versioned profile JSON through the shared `StudentProfileViewModel`, Desktop previews the same decoded domain model,
and a shared Room DAO replaces facts plus programme-scoped assertions in one transaction. The imported profile ID is
rebased to the active local profile; plans, saved doors, constraints and the current catalog remain local to the
receiving device. Repository tests cover all seven `FactValue` variants and force a mid-import SQLite failure to
verify rollback.

Local evidence attachments use the same shared domain metadata, Room schema, validation policy, confirmation
ViewModel, and Compose UI on Android and Desktop. Platform storage copies accepted images/PDF/TXT/DOC/DOCX files
into FileKit's app-private files directory, while Room keeps only a relative storage key plus name, MIME type and
byte size. Replacing or deleting evidence cleans up the old file after the database write succeeds; failed database
writes remove the newly copied file. Profile JSON deliberately strips the private storage key and never embeds the
binary. Tests cover type/size rejection, entity/export round-trips, migration v3→v4, confirmation persistence and
cleanup behavior.

## 100-opportunity performance characterization

`ScenarioEnginePerformanceCharacterizationTest` provides a reproducible JVM workload with exactly 100 distinct
opportunity IDs. The fixture includes `ALL` and `ANY` rule graphs, missing facts, manual checks, known/TBA/missing
deadlines, saved opportunities, planning constraints, and a multi-fact in-memory scenario overlay. It asserts that
eligibility receipts, scenario results, and the Action Frontier are identical across repeated evaluations and that
the scenario never mutates the actual profile.

Run from `MobileApp/` on the frozen verification host:

```bash
./gradlew :shared:jvmTest \
  --tests 'io.github.vietdsk6.porta.domain.usecase.ScenarioEnginePerformanceCharacterizationTest' \
  --info --rerun-tasks
```

The runner performs 10 untimed warm-up passes, then prints median and p95 for 30 measured passes of:

- eligibility evaluation across 100 opportunities;
- scenario re-evaluation across the same 100 opportunities;
- the complete constraint-aware Action Frontier calculation.

The output is prefixed with `PORTA_PERF` and includes the OS, architecture, and JDK version. Timing is intentionally
not a strict CI assertion: hosted-runner scheduling would make that gate flaky. The working-tree verification below
demonstrates the PRD's under-100-ms target on the development machine. Repeat the same command after the submission
commit is frozen and replace the commit row before final delivery.

| Frozen-run field | Recorded value |
|---|---|
| Commit | Working tree based on `b15f1fa` (final frozen-commit rerun pending) |
| Machine / OS / architecture | macOS 27.2 / aarch64 / 10 logical processors |
| JDK | 21.0.11 |
| Eligibility — median / p95 | 0.127 ms / 0.204 ms |
| Scenario — median / p95 | 0.434 ms / 0.574 ms |
| Action Frontier — median / p95 | 1.656 ms / 2.226 ms |

The ratio should therefore be used as a provenance cross-check beside the architecture and runnable demo, not as a
substitute for them.
