# Porta shared Kotlin evidence

> Snapshot date: 23 October 2026  
> Baseline: `bd5fd5d` — the last repository commit before Porta work began in `6be9ec1`  
> Measured Kotlin head: `04a897d`

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
| Shared production | 57 | 8,409 |
| Platform production | 0 | 0 |
| Kotlin data/fixtures | 2 | 968 |
| Shared tests | 7 | 948 |
| Platform-run tests | 14 | 1,560 |
| Excluded added Kotlin files | 12 | — |

Within this deliberately narrow production sample, the shared proportion is **8,409 / 8,409 = 100.0%**.

That number means all newly added, explicitly allowlisted Porta production Kotlin is currently in `commonMain`.
It does **not** mean 100% of the repository is shared, nor that platform code is unnecessary. Android and Desktop
still use target launchers, drivers, packaging, and platform services inherited from the starter kit; those inherited
lines are excluded rather than claimed as Porta work.

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

The ratio should therefore be used as a provenance cross-check beside the architecture and runnable demo, not as a
substitute for them.
