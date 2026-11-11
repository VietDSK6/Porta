# Fresh-clone and packaged-app verification

Run from a new clone of the public repository at the frozen commit. Record failures once, fix the cause, and add
only troubleshooting that was actually encountered. Do not repeatedly rerun a failing command without diagnosis.

## Environment record

```text
Commit SHA: 5a17882 (public evidence snapshot; product Kotlin head d3d34dc)
Clone URL: https://github.com/VietDSK6/Porta.git, cloned with credential helper disabled
Host OS/version: macOS 27.2 (26B123), Apple Silicon
JDK distribution/version: JetBrains Runtime 21.0.11+10-b1163.116
Android Studio/version: not used for automated verification
Android SDK/API: existing local SDK configuration; manual device/API verification pending
Android device/emulator: not used; manual launch pending
Desktop package type: macOS DMG
Verification date/time: 2026-11-02 15:42 +07
Verifier: Codex public-clone automated gates; entrant observed packaged Desktop launch past splash
```

## Automated paths

| Check | Command | Result | Evidence / note |
|---|---|---|---|
| Formatting | `./gradlew spotlessCheck` | PASS | Credential-free public clone; 1 s with shared Gradle cache. |
| Shared/JVM/UI tests | `./gradlew :shared:jvmTest :shared:testAndroidHostTest` | PASS | Public clone; 236 JVM + 94 Android-host tests, 3 s with shared cache. |
| Android debug build | `./gradlew :androidApp:assembleDebug` | PASS | Public clone; APK assembled in 2 s with shared cache. Manual device launch remains below. |
| Desktop package | `./gradlew :desktopApp:packageDistributionForCurrentOS` | PASS | Public clone; DMG assembled in 34 s. Runtime image explicitly contains `jdk.unsupported` and `sun.misc.Unsafe`. |
| JS/Wasm compatibility | `./gradlew :shared:compileKotlinJs :shared:compileKotlinWasmJs` | PASS | Public clone; both shared targets compiled in 1 s with shared cache. |

### Later local test evidence

The table above is the historical **2 November public-clone run** at `5a17882`; its 236 JVM and 94 Android-host
counts remain accurate for that snapshot. The 11 November local P0 run produced JUnit XML results with **287 JVM** and
**104 Android-host** tests, with zero failures, errors, or skips in those result files. It also passed `spotlessCheck`
and `:androidApp:assembleDebug`. This is not a new public-clone verification. Re-run the scoped gates on the frozen
submission commit and record its SHA and counts here before submission.

### Startup regression note

The first packaged Desktop check exposed `NoClassDefFoundError: sun/misc/Unsafe` while DataStore restored onboarding,
which left `isLoading` on the splash. The package now includes `jdk.unsupported`, the fresh runtime image was
inspected with `jimage`, and onboarding has a tested failure path that clears loading even if preference restore
fails. The entrant then observed the rebuilt package launch beyond the splash. Full keyboard and screen-reader
exercise is still intentionally unchecked below.

## Android manual path

- [ ] Fresh install launches offline without an account or service configuration.
- [ ] Fictional sample reaches a meaningful What-if preview in under 30 seconds; record measured time.
- [ ] System Back returns through detail/preview/setup without losing or duplicating navigation state.
- [ ] Portrait, landscape, and a tablet-sized viewport keep primary actions visible.
- [ ] 200% font scaling preserves all trust disclosures and primary actions.
- [ ] TalkBack announces conclusion, source/freshness, What-if summary, and buttons meaningfully.
- [ ] Pack import rejects an invalid file without partial changes and previews a valid update before activation.
- [ ] Export warning names included sensitive categories; focused clear and full reset remove the stated scopes only.

## Desktop packaged-app path

- [ ] Native package installs/opens on the recorded OS with networking disabled.
- [ ] Tab/Shift-Tab reaches every P0 control in a logical order with visible focus.
- [ ] Navigation rail, Frontier selection, constraints, What-if receipt, source access, and Pack Inspector are usable keyboard-only.
- [ ] Narrow and 1280×800 windows preserve content, source disclosures, and primary actions.
- [ ] 200% text or equivalent OS scaling has no clipped critical controls.
- [ ] Screen reader announces navigation, conclusions, changed-count summary, and expanded rule details meaningfully.
- [ ] Import/export works using files outside the project directory.
- [ ] Relaunch preserves profile, saves, planning constraints, and the active plan.

## Content/source verification

- [ ] A second reader has checked all 15 bundled records against the linked official source.
- [ ] Every hard rule has a source note and last-reviewed date.
- [ ] TBA/rolling deadlines do not reuse an older date.
- [ ] Manual, constituency-specific, legal, and soft conditions remain non-deterministic.
- [ ] At least five curated milestones produce legitimate state deltas for the sample fixture.
- [ ] Demo wording never implies admission, funding, selection, endorsement, or guaranteed eligibility.
