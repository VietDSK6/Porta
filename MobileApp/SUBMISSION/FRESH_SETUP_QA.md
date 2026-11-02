# Fresh-clone and packaged-app verification

Run from a new clone of the public repository at the frozen commit. Record failures once, fix the cause, and add
only troubleshooting that was actually encountered. Do not repeatedly rerun a failing command without diagnosis.

## Environment record

```text
Commit SHA: d3d34dc
Clone URL: local no-hardlink clone of the repository (public URL still blocked)
Host OS/version: macOS 27.2 (26B123), Apple Silicon
JDK distribution/version: JetBrains Runtime 21.0.11+10-b1163.116
Android Studio/version: not used for automated verification
Android SDK/API: existing local SDK configuration; manual device/API verification pending
Android device/emulator: not used; manual launch pending
Desktop package type: macOS DMG
Verification date/time: 2026-11-02 15:42 +07
Verifier: Codex automated gates; entrant observed packaged Desktop launch past splash
```

## Automated paths

| Check | Command | Result | Evidence / note |
|---|---|---|---|
| Formatting | `./gradlew spotlessCheck` | PASS | Fresh clone; 11 s. |
| Shared/JVM/UI tests | `./gradlew :shared:jvmTest :shared:testAndroidHostTest` | PASS | Fresh clone; 236 JVM + 94 Android-host tests in the source-workspace run, same test tasks green in clone. |
| Android debug build | `./gradlew :androidApp:assembleDebug` | PASS | Fresh clone; APK assembled in 10 s. Manual device launch remains below. |
| Desktop package | `./gradlew :desktopApp:packageDistributionForCurrentOS` | PASS | Fresh clone; DMG assembled in 34 s. Runtime image explicitly contains `jdk.unsupported` and `sun.misc.Unsafe`. |
| JS/Wasm compatibility | `./gradlew :shared:compileKotlinJs :shared:compileKotlinWasmJs` | PASS | Fresh clone; both shared targets compiled in 10 s. |

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
