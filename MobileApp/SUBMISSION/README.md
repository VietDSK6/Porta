# Porta submission package

This directory keeps the final, judge-facing materials separate from implementation notes. Nothing in this folder
should claim a fact that cannot be demonstrated by the tagged submission commit or confirmed by the entrant.

## Current status — 12 November 2026

The public-clone results below are **historical evidence from 2 November** at `5a17882`. They do not verify the
current P5 code commit. The P5 code is locally committed at `dc4e701`, and its scoped gates and visual preview review
passed. A fresh clone of the eventual public submission commit, device and packaged-app checks, and final media
capture remain pending. Web build work is deferred at the
entrant's request; Web is not a declared submission target.

| Item | Status | Exit condition |
|---|---|---|
| Entrant eligibility | USER CONFIRM | Confirm age/majority, enrollment, residency/sanctions, employment/affiliation, and ability to accept the prize against the live rules. |
| Individual/one entry | USER CONFIRM | Confirm solo submission and no second entry. |
| GitHub repository | PUBLIC; FROZEN COMMIT PENDING | `https://github.com/VietDSK6/Porta`; the 2 November credential-free clone resolved `main` to `5a17882`. Recheck after the final push. |
| Root README | HISTORICAL PUBLIC CHECK | The 2 November public README presented Porta, both declared targets, provenance, trust boundaries, and reproducible commands. Recheck the frozen commit. |
| License | HISTORICAL PUBLIC CHECK | Root `LICENSE` was visible in the 2 November credential-free clone; preserve existing notices and recheck the frozen commit. |
| Android build | HISTORICAL PUBLIC-CLONE PASS | `:androidApp:assembleDebug` passed in the 2 November clone; final build and device launch remain pending. |
| JVM Desktop package | HISTORICAL PUBLIC-CLONE PASS | The 2 November DMG built and launched past startup after the `jdk.unsupported` fix; final package and keyboard/screen-reader QA remain pending. |
| Current P5 code | LOCAL COMMIT; GATES PASS | `dc4e701`; scoped tests, Android APK, Desktop DMG, Roborazzi previews, and 100-opportunity timing are recorded locally. Public-clone verification remains pending. |
| Web build | DEFERRED | The entrant requested that Web build work wait while Android, Desktop, and other app work proceeds. |
| Essay | NEEDS ENTRANT FACTS | `ESSAY.md` is a 300-word structural draft with four factual placeholders. Replace them truthfully and re-run the word-count command. |
| Screencast | 3:30 SCRIPT READY | Record and review a 3–5 minute video using the Route-first `DEMO_SCRIPT.md`. |
| Final media | CAPTURE REQUIRED | Capture real Android and Desktop output using `MEDIA_CHECKLIST.md`; do not substitute design mockups. |
| Source audit | IN PROGRESS | A second reader must check all 15 records and all TBA/current-cycle dates. |
| Submission form | TODO | Submit before 7 December 2026, 23:59 CET and save the receipt outside the repository. |

## Freeze procedure

1. Replace every factual placeholder in `ESSAY.md` and keep the essay at exactly 300 words.
2. Complete every mandatory row in `FRESH_SETUP_QA.md` from a clean clone of the public repository.
3. Complete the independent source review in `../OPPORTUNITY_PACK_SOURCES.md`.
4. Record the screencast and capture final screenshots from the frozen build.
5. Audit screenshots, fonts, icons, logos, voices, and third-party marks for sufficient rights.
6. Update `../CONTEST_SCORECARD.md` with only evidence from the frozen commit.
7. Tag the verified commit, push it, and verify README links and clone instructions while signed out.
8. Submit the repository URL and 300-word essay through the official form.
9. Save the form receipt, submitted essay, repository commit SHA, and video URL in private records.

## Final release record

Fill only after the public snapshot exists:

```text
Repository URL: https://github.com/VietDSK6/Porta
Verified public commit SHA: [frozen commit pending]
Release/tag:
Android verification device/API:
Desktop verification OS/version:
Screencast URL:
Submission timestamp (CET):
Receipt location:
```
