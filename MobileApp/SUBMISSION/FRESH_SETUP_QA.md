# Fresh-clone and packaged-app verification

Run from a new clone of the public repository at the frozen commit. Record failures once, fix the cause, and add
only troubleshooting that was actually encountered. Do not repeatedly rerun a failing command without diagnosis.

## Environment record

```text
Commit SHA:
Clone URL:
Host OS/version:
JDK distribution/version:
Android Studio/version:
Android SDK/API:
Android device/emulator:
Desktop package type:
Verification date/time:
Verifier:
```

## Automated paths

| Check | Command | Result | Evidence / note |
|---|---|---|---|
| Formatting | `./gradlew spotlessCheck` |  |  |
| Shared/JVM/UI tests | `./gradlew :shared:jvmTest :shared:testAndroidHostTest` |  |  |
| Android debug build | `./gradlew :androidApp:assembleDebug` |  |  |
| Desktop package | `./gradlew :desktopApp:packageDistributionForCurrentOS` |  |  |
| JS/Wasm compatibility | `./gradlew :shared:compileKotlinJs :shared:compileKotlinWasmJs` |  |  |

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
