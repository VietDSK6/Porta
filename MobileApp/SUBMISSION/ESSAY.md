# 300-word contest essay

The background and experience below use the entrant's August 2026 CV: PTIT Information Technology, AISOFT,
PTIT S-Link, the public-service Zalo Mini App, and Myosotis. The entrant chose Myosotis and accessibility as the
relevant personal interest. Porta's idea and implementation details come from the project. The entrant should
check that the employment and enrollment facts are still accurate before submitting. Do not submit this note or
the markers below.

Count only the text between the markers:

```bash
sed -n '/^<!-- ESSAY_START -->$/,/^<!-- ESSAY_END -->$/p' SUBMISSION/ESSAY.md | sed '1d;$d' | wc -w
```

<!-- ESSAY_START -->
I study Information Technology at the Posts and Telecommunications Institute of Technology (PTIT) and work as a mobile developer at AISOFT. I build React Native and TypeScript workflows for PTIT S-Link and a public-service Zalo Mini App. Working on Myosotis, an accessibility-focused app for people with Alzheimer's disease, shaped my interest in clear, inclusive software.

That work led me to a question: scholarships, internships, contests, and study programmes are plentiful, but which milestones should someone tackle with limited time, effort, and money? Porta's Route planner orders up to four controllable steps and reevaluates published checks at each step's end date. It shows improved checks, doors opened, and doors lost to deadlines. An Action Frontier compares non-dominated moves.

Students inspect why doors need action or verification, change Route limits, and preview milestones without changing their saved profile. Every conclusion traces to encoded rules and an official source. Completing a plan never silently proves a fact; updating the actual profile requires separate confirmation, with optional evidence. Missing information stays unknown, manual checks stay manual, and soft selection criteria never become eligibility scores. Linh's fictional profile improves checks across 15 sourced doors, yet opens zero.

Porta uses Kotlin 2.4.10 and Compose Multiplatform. Shared Kotlin contains deterministic rules, scenarios, Route planning, pack validation, Room persistence, ViewModels, and interface code. Android supports profile and progress updates; JVM Desktop offers a route map, comparison workspace, and pack inspector. A JVM CLI exports, validates, evaluates, and plans with the engine. Common, JVM, and Android-host tests exercise trust boundaries and behavior.

Porta works locally without accounts, analytics, subscriptions, or an AI key. Optional drafting uses offline examples; with a personal key, Gemini may propose source-quoted rules for human review and separate pack approval. AI never decides eligibility or predicts selection. Porta maps assessed possibilities across Android and Desktop.
<!-- ESSAY_END -->
