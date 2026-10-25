# Porta bundled opportunity pack — source register

> Last manual source pass: **21 October 2026**. This register documents Porta's independent
> interpretation of public provider pages. It is not an endorsement, partnership, or guarantee of
> eligibility. Always confirm the live source before acting.

> Wave B vertical-slice recheck: **25 October 2026**. Erasmus Mundus, DAAD EPOS,
> JJ/WBGSP and OECD were read again against the official sources for the scripted scenarios below.
> Other records retain their 21 October review date.

## Encoding policy

- `ATOMIC` is used only when the published condition maps directly to a typed Porta fact.
- `MANUAL CHECK` is used for course-, vacancy-, constituency-, citizenship-, conflict- and
  history-specific rules that Porta cannot evaluate safely.
- Soft selection language never contributes to eligibility.
- `TBA` and rolling deadlines never inherit a date from an older cycle.
- Action duration, effort and cost are labelled Porta planning estimates; providers do not publish
  those estimates.

## Source-backed vertical-slice fixtures

These fixtures test check-level progress, not admission probability or provider approval. A changed
typed check may still leave manual conditions or deadline uncertainty, so none of these fixtures is
allowed to claim that an applicant will be accepted.

| Fixture | Baseline → scenario | Exact encoded change | What must remain visible |
|---|---|---|---|
| First-degree milestone | `degreeCompleted=false → true` | The published first-degree check changes for Erasmus Mundus and DAAD EPOS. The JJ/WBGSP degree-presence check also changes, but its three-year timing rule remains manual and its known deadline may make a new completion plan infeasible. | Programme-specific entry rules, degree timing, course fit and TBA deadlines remain unresolved. |
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
| Chevening Scholarship | [Eligibility criteria](https://www.chevening.org/resource-hub/guidance/eligibility/) | Degree completion is typed. The 2,800-hour definition, citizenship, return commitment, conflicts and offers remain manual. | Next cycle dates are not copied from a closed cycle. |
| Rhodes Scholarship | [Applications](https://www.rhodeshouse.ox.ac.uk/scholarships/applications/) | Entire eligibility decision is manual because the official checker varies by constituency. | Constituency-specific. |
| Schwarzman Scholars | [Application instructions](https://www.schwarzmanscholars.org/admissions/application-instructions/) | Published age band and degree completion are typed; English evidence/exemption remains manual. | Class of 2028–2029; exact close date still TBA. |
| France Excellence Eiffel | [Campus France programme page](https://www.campusfrance.org/en/the-france-excellence-eiffel-scholarship-program) | Nationality, age band, field and institution nomination remain manual. | Campus France institution deadline: 8 Jan 2027; institutions may close earlier. |
| DAAD EPOS | [Official scholarship record](https://www2.daad.de/deutschland/stipendium/datenbank/en/21148-scholarship-database.pdf?detail=50076777) | Degree and 24-month duration are typed; experience relevance/timing, country, language and course fit remain manual. | Course-specific 2027–2028 deadlines. |
| JJ/WBGSP | [2027 application cycle](https://www.worldbank.org/en/programs/scholarships/jj-wbgsp) | Degree and 36-month duration are typed; nationality, work definition/timing and participating-program admission remain manual. | Window 1 closes 26 Feb 2027. |
| OECD Internship Programme | [Internships](https://www.oecd.org/en/about/careers/internships.html) | Full-time enrollment and minimum-period availability are typed; field and language fit remain manual. | Rolling applications. |
| UNICEF Internship Programme | [Internships](https://www.unicef.org/careers/internships) | Age and current enrollment are typed; recent-graduate, language and family-conflict branches remain manual. | Vacancy-specific, throughout the year. |
| World Bank Treasury Summer Internship | [Student careers](https://treasury.worldbank.org/en/about/unit/treasury/about/student-and-graduate-careers) | Enrollment and full-period availability are typed; degree stage, good standing, field and concurrent work remain manual. | 2027 application close date must be confirmed live. |
| ESA Student Internships | [Student internships](https://www.esa.int/About_Us/Careers_at_ESA/Student_Internships2) | Enrollment and placement availability are typed; citizenship and vacancy rules remain manual. | Vacancy-specific. |
| CERN openlab Summer Student Programme | [Programme page](https://openlab.web.cern.ch/cern-openlab-summer-student-programme/) | Student status and nine-week availability are typed; study duration, field and computing profile remain manual. | 2027 call announced for Nov 2026; closing date TBA. |
| Google Summer of Code | [Official FAQ](https://developers.google.com/open-source/gsoc/faq) | Minimum age is typed; residence, work eligibility, beginner status and prior participation remain manual. | Next contributor timeline TBA. |
| Swift Student Challenge | [2026 terms](https://developer.apple.com/swift-student-challenge/policy/) | Current enrollment is one typed education branch; country age, other education branches, employment, registration and award history remain manual. | Archived 2026 record; deadline was 28 Feb 2026. |

## Reverification checklist

For each new pack version:

1. Open every official URL and record the new checked date.
2. Compare eligibility wording, cycle, deadline and application route.
3. Replace changed records instead of silently mutating a historical cycle.
4. Test at least one passing, failing and unknown/manual profile where the rule shape changed.
5. Keep expired records archived and out of current recommendations.
