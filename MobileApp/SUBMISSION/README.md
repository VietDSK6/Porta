# Porta submission package

This directory keeps the final, judge-facing materials separate from implementation notes. Nothing in this folder
should claim a fact that cannot be demonstrated by the tagged submission commit or confirmed by the entrant.

## Current status — 2 November 2026

| Item | Status | Exit condition |
|---|---|---|
| Entrant eligibility | USER CONFIRM | Confirm age/majority, enrollment, residency/sanctions, employment/affiliation, and ability to accept the prize against the live rules. |
| Individual/one entry | USER CONFIRM | Confirm solo submission and no second entry. |
| GitHub repository | BLOCKED | Create the entrant-owned public repository, push the frozen commit, and verify it while signed out. |
| Root README | READY LOCALLY | Root README presents Porta, both declared targets, provenance, trust boundaries, and reproducible commands. |
| License | PASS LOCALLY | Root `LICENSE` is MIT; preserve existing notices and verify it is visible in the public repository. |
| Android build | PASS LOCALLY | `:androidApp:assembleDebug` succeeds; fresh-clone launch remains manual. |
| JVM Desktop package | PASS LOCALLY | `:desktopApp:packageDistributionForCurrentOS` succeeds; packaged keyboard/screen-reader QA remains manual. |
| Essay | NEEDS ENTRANT FACTS | `ESSAY.md` is a 300-word structural draft with four factual placeholders. Replace them truthfully and re-run the word-count command. |
| Screencast | SCRIPT READY | Record and review a 3–5 minute video using `DEMO_SCRIPT.md`. |
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
Repository URL:
Frozen commit SHA:
Release/tag:
Android verification device/API:
Desktop verification OS/version:
Screencast URL:
Submission timestamp (CET):
Receipt location:
```
