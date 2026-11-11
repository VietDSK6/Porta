# 300-word contest essay draft

Replace each single-token placeholder with truthful entrant information. If a replacement contains spaces, adjust
other prose so the body remains exactly 300 words. Do not submit the explanatory text or markers below.

Required facts:

- `[EDUCATION]` — degree/program and institution context.
- `[CODING_EXPERIENCE]` — truthful languages, platforms, projects, or work experience.
- `[HOBBY]` — a genuine relevant hobby or interest.
- `[MOTIVATION]` — the personal observation that led to Porta.

Count only the text between the markers:

```bash
sed -n '/^<!-- ESSAY_START -->$/,/^<!-- ESSAY_END -->$/p' SUBMISSION/ESSAY.md | sed '1d;$d' | wc -w
```

<!-- ESSAY_START -->
I am [EDUCATION], with coding experience in [CODING_EXPERIENCE]. Outside formal study, [HOBBY] has shaped how I approach difficult problems: make the evidence visible, separate assumptions from facts, and build small tools that help people decide confidently. [MOTIVATION] led me to create Porta for the Kotlin Student Coding Competition.

Students often find many scholarships, internships, contests, and study programmes, yet still cannot answer a practical question: which realistic next milestone creates the most useful opportunities under limited time, effort, and money? Existing search and matching products may show lists, scores, or profile gaps. Porta instead evaluates a curated catalog against typed published rules, explains every assessed check, and automatically constructs an Action Frontier of non-dominated, controllable milestones.

A student can inspect why a door needs action or verification, change planning constraints, and preview one milestone without modifying the saved profile. Porta shows only the opportunity conclusions and individual rules that actually change. Completing a plan never silently proves a fact; updating the real profile requires a separate confirmation, with optional evidence metadata. Missing information stays unknown, manual requirements stay manual, and soft selection criteria never become eligibility scores.

Porta is built with Kotlin 2.4.10 and Compose Multiplatform. Shared Kotlin contains the rule graph, deterministic evaluator, scenario engine, Pareto frontier, Route planner, pack validation, Room persistence, repositories, ViewModels, UI state, and most interface code. Android supports quick profile and progress updates, while JVM Desktop provides a wider comparison and inspection workspace. Common, JVM, and Android-host tests exercise the same trust rules and product behavior.

Porta works locally without accounts, analytics, subscriptions, or an AI key. Optional drafting uses offline examples; with a personal key, Gemini proposes source-quoted rules for human approval. It never decides eligibility or predicts selection. Porta gives students a source-linked map of realistic possibilities across phones and desktop computers.
<!-- ESSAY_END -->
