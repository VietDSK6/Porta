# Porta bundled opportunity pack — source register

> Last complete manual source pass: **21 October 2026**. This register documents Porta's independent
> interpretation of public provider pages. It is not an endorsement, partnership, or guarantee of
> eligibility. Always confirm the live source before acting.

> Wave B vertical-slice recheck: **25 October 2026**. Erasmus Mundus, DAAD EPOS,
> JJ/WBGSP and OECD were read again against the official sources for the scripted scenarios below.
> Other records retain their 21 October review date.

> T18 selective source audit: **2 November 2026**. The five programmes in the table below were
> rechecked against official pages. This is not a revalidation of every bundled record or a claim
> that a programme-specific vacancy/course is open. Two material catalog warnings are recorded
> below for JJ/WBGSP and Chevening. Pack metadata retains the **21 October pack-wide review date**;
> each refreshed record carries its own 2 November date.

> Selective 14 November 2026 refresh: the [CERN openlab 2027 listing](https://jobs.smartrecruiters.com/cern/744000159596524)
> publishes a **21 January 2027, 23:59 Geneva time** application deadline and confirms the current call.
> Porta now uses that date for the CERN record. Its six-semester study requirement, prior CERN
> participation limits, English knowledge and computing-profile judgment still require source review;
> confirming nine-week availability alone does not pass the entire eligibility root. The
> [Campus France Eiffel timeline](https://www.southafrica.campusfrance.org/france-excellence-eiffel-scholarship)
> lists **6 January 2027** as the *institution's* nomination deadline. A student's deadline is set
> by their chosen institution, so Porta now leaves the student deadline TBA instead of showing the
> former 8 January date. This is a selective update, not a second reading of every record.

## Encoding policy

- `ATOMIC` is used only when the published condition maps directly to a typed Porta fact.
- `MANUAL CHECK` is used for course-, vacancy-, constituency-, citizenship-, conflict- and
  history-specific rules that Porta cannot evaluate safely.
- Soft selection language never contributes to eligibility.
- `TBA` and rolling deadlines never inherit a date from an older cycle.
- Action duration, effort and cost are labelled Porta planning estimates; providers do not publish
  those estimates.

## T18 source gate — 2 November 2026

The five records below are the deeper, current or announced-cycle candidates for this data slice.
Each row names exactly what a typed Porta fact can check and what must remain a manual check.
An action changing a **published check** must never be counted as full programme eligibility.

| Record and official section | Current cycle / application timing | Published typed check(s) suitable for Porta | Manual boundary and source note |
|---|---|---|---|
| [Erasmus Mundus Joint Masters — “What you need”; “When to apply”](https://erasmus-plus.ec.europa.eu/opportunities/individuals/students/erasmus-mundus-joint-masters) | 2027 intake is programme-specific; the EU page says most applications fall between October and January. | A first degree is one route; final-year applicants can apply if they graduate before the master starts. | Do not infer a universal deadline, IELTS score or consortium admission. The final-year/recognised-equivalent routes remain manual until a programme is selected. |
| [DAAD EPOS — “Application requirements”; “Application Procedure”](https://www2.daad.de/deutschland/stipendium/datenbank/en/21148-scholarship-database?detail=50076777) and [2027/28 course list](https://static.daad.de/media/daad_de/pdfs_nicht_barrierefrei/in-deutschland-studieren-forschen-lehren/daad_epos_deadlines.pdf) | 2027/28 intake, with a separate deadline for each course; no pack-wide closing date. | Related first degree and at least two years of professional experience are published checks. | Relevance and **post-degree timing**, course-specific language/admission, academic standing and current country scope cannot be inferred from generic `EXPERIENCE_MONTHS`. DAAD's linked country PDF is labelled valid 2024–25, so it must be rechecked before coding nationality. |
| [Schwarzman Scholars — “Language Skills”; “Next Selection Cycle”; “Check Your Eligibility”](https://www.schwarzmanscholars.org/admissions/application-instructions/) and [English FAQ](https://www.schwarzmanscholars.org/frequently-asked-questions/) | Class of 2028–29; global application announced for April–September 2027, Chinese-citizenship route March–May 2027; exact closing day is not yet published. | DOB against the 18–28 band on 1 August 2028, undergraduate-degree completion by that date, and **IELTS ≥ 7** as one accepted English-test route. | IELTS is an **alternative**, not a universal requirement: TOEFL, DET, Cambridge, native English and qualifying English-medium study routes exist. Test validity, route and evidence need manual confirmation; a 6.5 score alone cannot imply rejection. |
| [OECD Internship Programme — “Who can apply?”; “Internship duration”](https://www.oecd.org/en/about/careers/internships.html) | Rolling applications; internship duration starts at one month and actual start depends on selection. | Full-time degree enrolment throughout the placement and availability for at least one month. | Availability must be confirmed for **this programme**, not copied to another internship. Field, language, nationality pathway and team selection remain manual. |
| [UNICEF Internship Programme — “Requirements”; “How to apply”](https://www.unicef.org/careers/internships) | Vacancies appear throughout the year; each has its own requirements and dates. | Age ≥ 18 and the current-enrolment branch are directly checkable. | Graduation within two years is an alternative to enrolment. Language, relatives/conflicts, vacancy-specific duties and selection remain manual. The global page says professional experience receives additional consideration, not a hard threshold. |

### Cross-opportunity action candidate

Completing a first degree is a real, shared **typed check** for [Erasmus Mundus](https://erasmus-plus.ec.europa.eu/opportunities/individuals/students/erasmus-mundus-joint-masters), [DAAD EPOS](https://www2.daad.de/deutschland/stipendium/datenbank/en/21148-scholarship-database?detail=50076777), and [Schwarzman Scholars](https://www.schwarzmanscholars.org/admissions/application-instructions/). A scenario `degreeCompleted=false → true` may therefore change several check receipts. It does **not** prove a student can apply now: Erasmus has a final-year alternative, DAAD requires relevant post-degree experience and course fit, and Schwarzman requires completion by **1 August 2028** rather than completion on the day the profile is edited. The app should name the changed check and keep timing/manual conditions visible.

In bundled pack **version 4**, the 2 November scenario produces three deadline-feasible current-cycle check changes for this milestone: Erasmus, DAAD and Schwarzman. It produces **zero newly open doors**. Chevening is archived and the JJ/WBGSP Window 1 deadline is too soon for the pack's disclosed degree-planning estimate. The estimate is a Porta assumption, not a provider timeline.

### Time-sensitive exclusions and corrections

- The [World Bank's official **2027 eligible-country list**](https://www.worldbank.org/en/brief/2026/07/15/countries-eligible-for-jjwbgsp-scholarship) does **not** list Viet Nam. The [2027 JJ/WBGSP programme page](https://www.worldbank.org/en/programs/scholarships/jj-wbgsp) also requires a degree earned at least three years before the deadline, three years of qualifying paid development work in the last six years, and unconditional admission to a participating programme. Keep the nationality branch manual unless the exact 2027 list is encoded. Do not use JJ/WBGSP as a positive showcase recommendation for a Viet Nam profile merely because a degree/experience check changes. Window 1 is 18 January–26 February 2027 and Window 2 is 29 March–21 May 2027.
- **2027 JJ/WBGSP country-code mapping is not lossless yet.** Porta's single-nationality picker uses ISO 3166-1 alpha-2 and has `PS` for Palestine but no Kosovo entry. The World Bank list names **“West Bank and Gaza”** and **“Kosovo”**; it also forbids dual citizenship of a developed country. An unqualified `NATIONALITY IN_SET` over current profile values could wrongly fail an applicant in a listed territory or wrongly pass a person with disqualifying second citizenship. Keep this condition manual until special-status names and multiple citizenship can be represented, or label a narrower country-list check explicitly as partial.
- The JJ/WBGSP warning is displayed in its manual check and readiness note. Porta does not automatically classify every Viet Nam citizen from the single stored nationality field; the user must check all citizenships and the official 2027 list. This also means a distant-horizon plan may still show a **partial** degree or experience check change, never a full eligibility conclusion.
- The [Chevening application timeline](https://www.chevening.org/scholarships/application-timeline/) says the 2027–28 scholarship application opened in August 2026 and **closed in October 2026**. Its [eligibility page](https://www.chevening.org/resource-hub/guidance/eligibility/) requires 2,800 eligible **post-degree work hours** and undergraduate study completed at least two years before the application deadline. A generic 24-month fact is not equivalent, and “next cycle · dates to be confirmed” must not describe the already-closed 2027–28 window. Verify the next published cycle rather than inventing a reopening date.

## Source-backed vertical-slice fixtures

These fixtures test check-level progress, not admission probability or provider approval. A changed
typed check may still leave manual conditions or deadline uncertainty, so none of these fixtures is
allowed to claim that an applicant will be accepted.

| Fixture | Baseline → scenario | Exact encoded change | What must remain visible |
|---|---|---|---|
| First-degree milestone | `degreeCompleted=false → true` | The published first-degree check changes for Erasmus Mundus, DAAD EPOS and Schwarzman. The JJ/WBGSP degree-presence check also changes in an unrestricted scenario, but its three-year timing rule remains manual and its known deadline may make a new completion plan infeasible. | Programme-specific entry rules, degree timing, country, course fit and TBA deadlines remain unresolved. |
| Experience milestone | `experienceMonths=6 → 36` | The 24-month DAAD EPOS and 36-month JJ/WBGSP duration checks change from fail to pass. | Relevance, paid/post-degree timing, counting windows, country and admission rules remain manual. |
| OECD availability confirmation | no OECD assertion → confirmed for the OECD availability rule | Only `oecd-availability` changes; the assertion is scoped to that programme and rule fingerprint. | Field/language fit and the rolling application source check remain unresolved; no other programme inherits the assertion. |

Official wording rechecked for these fixtures:

- [Erasmus Mundus — What you need](https://erasmus-plus.ec.europa.eu/opportunities/individuals/students/erasmus-mundus-joint-masters): bachelor degree, final-year, or recognised equivalent route; programme-specific requirements and dates.
- [DAAD EPOS — Requirements](https://www2.daad.de/deutschland/stipendium/datenbank/en/21148-scholarship-database.pdf?detail=50076777): related bachelor degree and at least two years of relevant professional experience after the first degree.
- [JJ/WBGSP — Who is Eligible to Apply?](https://www.worldbank.org/en/programs/scholarships/jj-wbgsp): bachelor degree earned at least three years before the deadline and three years of qualifying paid development-related employment in the stated window.
- [OECD internships — Who can apply?](https://www.oecd.org/en/about/careers/internships.html): full-time degree enrolment for the placement and availability for at least one month.

## Records

| Record | Official source | Encoded boundary | Cycle/deadline note |
|---|---|---|---|
| Kotlin Student Coding Competition | [Official rules](https://kotlinconf.com/contest-rules/) | Age and current enrollment are typed; recent enrollment, sanctions, employment and submission requirements remain manual. | Submission deadline: 7 Dec 2026, 23:59 CET. |
| Erasmus Mundus Joint Masters | [Erasmus+ student page](https://erasmus-plus.ec.europa.eu/opportunities/individuals/students/erasmus-mundus-joint-masters) | Completed bachelor is typed; the final-year alternative and consortium-specific entry rules remain manual. | Programme-specific; the EU page says most applications are Oct–Jan. |
| Chevening Scholarship | [Eligibility criteria](https://www.chevening.org/resource-hub/guidance/eligibility/); [application timeline](https://www.chevening.org/scholarships/application-timeline/) | Degree completion is typed. The 2,800 **post-degree work hours**, degree-age rule, citizenship, return commitment, conflicts and offers remain manual. | The 2027–28 application closed in October 2026; a future cycle date is unknown. |
| Rhodes Scholarship | [Applications](https://www.rhodeshouse.ox.ac.uk/scholarships/applications/) | Entire eligibility decision is manual because the official checker varies by constituency. | Constituency-specific. |
| Schwarzman Scholars | [Application instructions](https://www.schwarzmanscholars.org/admissions/application-instructions/); [English FAQ](https://www.schwarzmanscholars.org/frequently-asked-questions/) | Published age band and completed-degree check are typed. IELTS ≥ 7 is an encoded **alternative** branch; other tests, exemptions and degree completion by August 2028 remain manual. | Class of 2028–2029; global application April–September 2027, exact close date TBA. |
| France Excellence Eiffel | [Campus France campaign timeline](https://www.southafrica.campusfrance.org/france-excellence-eiffel-scholarship) | Nationality, age band, field and institution nomination remain manual. | Student deadline is institution-specific and TBA in Porta; institutions submit nominations by 6 Jan 2027. |
| DAAD EPOS | [Official scholarship record](https://www2.daad.de/deutschland/stipendium/datenbank/en/21148-scholarship-database.pdf?detail=50076777) | Degree and 24-month duration are typed; experience relevance/timing, country, language and course fit remain manual. | Course-specific 2027–2028 deadlines. |
| JJ/WBGSP | [2027 application cycle](https://www.worldbank.org/en/programs/scholarships/jj-wbgsp); [eligible-country list](https://www.worldbank.org/en/brief/2026/07/15/countries-eligible-for-jjwbgsp-scholarship) | Degree and 36-month duration are typed; nationality, work definition/timing and participating-program admission remain manual. **Viet Nam is absent from the 2027 country list.** | Window 1 closes 26 Feb 2027; Window 2 closes 21 May 2027. |
| OECD Internship Programme | [Internships](https://www.oecd.org/en/about/careers/internships.html) | Full-time enrollment and minimum-period availability are typed; field and language fit remain manual. | Rolling applications. |
| UNICEF Internship Programme | [Internships](https://www.unicef.org/careers/internships) | Age and current enrollment are typed; recent-graduate, language and family-conflict branches remain manual. | Vacancy-specific, throughout the year. |
| World Bank Treasury Summer Internship | [Student careers](https://treasury.worldbank.org/en/about/unit/treasury/about/student-and-graduate-careers) | Enrollment and full-period availability are typed; degree stage, good standing, field and concurrent work remain manual. | 2027 application close date must be confirmed live. |
| ESA Student Internships | [Student internships](https://www.esa.int/About_Us/Careers_at_ESA/Student_Internships2) | Enrollment and placement availability are typed; citizenship and vacancy rules remain manual. | Vacancy-specific. |
| CERN openlab Summer Student Programme | [2027 CERN listing](https://jobs.smartrecruiters.com/cern/744000159596524); [programme page](https://openlab.web.cern.ch/cern-openlab-summer-student-programme/) | Student status and nine-week availability are typed; six semesters of full-time study, enrollment through summer, prior CERN participation, English and computing profile need review. | 2027 call open; applications close 21 Jan 2027 at 23:59 Geneva time. |
| Google Summer of Code | [Official FAQ](https://developers.google.com/open-source/gsoc/faq) | Minimum age is typed; residence, work eligibility, beginner status and prior participation remain manual. | Next contributor timeline TBA. |
| Swift Student Challenge | [2026 terms](https://developer.apple.com/swift-student-challenge/policy/) | Current enrollment is one typed education branch; country age, other education branches, employment, registration and award history remain manual. | Archived 2026 record; deadline was 28 Feb 2026. |

## Reverification checklist

For a complete pack-wide review:

1. Open every official URL and record the new checked date.
2. Compare eligibility wording, cycle, deadline and application route.
3. Replace changed records instead of silently mutating a historical cycle.
4. Test at least one passing, failing and unknown/manual profile where the rule shape changed.
5. Keep expired records archived and out of current recommendations.

For a selective source refresh, retain the last complete pack-wide review date and record a newer date only on opportunities actually rechecked. Bump the bundled version so an existing bundled catalog upgrades on launch, while leaving a user-imported active catalog untouched.
