package io.github.vietdsk6.porta.data.source.local.pack

import io.github.vietdsk6.porta.domain.model.Actionability
import io.github.vietdsk6.porta.domain.model.CostBand
import io.github.vietdsk6.porta.domain.model.EffortLevel
import io.github.vietdsk6.porta.domain.model.FactKey
import io.github.vietdsk6.porta.domain.model.Operator
import io.github.vietdsk6.porta.domain.model.Opportunity
import io.github.vietdsk6.porta.domain.model.OpportunityCategory

/**
 * The offline catalog shipped with Porta.
 *
 * Every record is independently curated from the linked provider page. A manual rule is deliberate:
 * it means the official condition is real, but the current typed profile cannot evaluate it without
 * inventing an equivalence. Estimates on action templates are Porta planning aids, not provider data.
 */
internal object PortaContestPackData {
    const val VERIFIED_ON = "2026-10-21"

    fun create(): OpportunityPackDto = OpportunityPackDto(
        schemaVersion = OpportunityPackCodec.CURRENT_SCHEMA_VERSION,
        id = BundledOpportunityPackProvider.PACK_ID,
        name = "Porta curated opportunity pack",
        publisher = "Porta · independently curated from official sources",
        version = BundledOpportunityPackProvider.PACK_VERSION,
        lastCheckedOn = VERIFIED_ON,
        opportunities = listOf(
            kotlinStudentContest(),
            erasmusMundus(),
            chevening(),
            rhodes(),
            schwarzman(),
            eiffel(),
            daadEpos(),
            jjWbgsp(),
            oecdInternship(),
            unicefInternship(),
            worldBankTreasury(),
            esaInternship(),
            cernOpenlab(),
            googleSummerOfCode(),
            swiftStudentChallenge(),
        ),
    )

    private fun kotlinStudentContest(): OpportunityDto {
        val source = "https://kotlinconf.com/contest-rules/"
        val enrolledAction = enrollmentAction("kotlin-contest", source)
        return opportunity(
            id = "kotlin-student-coding-competition-2026",
            title = "Kotlin Student Coding Competition",
            provider = "Kotlin Foundation",
            category = OpportunityCategory.STUDENT_CONTEST,
            cycle = "2026 competition",
            sourceUrl = source,
            sourceSection = "Eligibility; How to Enter",
            deadline = DeadlineDto.Known("2026-12-07"),
            rule = all(
                "kotlin-root",
                atomic(
                    id = "kotlin-age",
                    factKey = FactKey.AGE,
                    operator = Operator.GREATER_THAN_OR_EQUAL,
                    expected = number(18.0, "years"),
                    explanation = "Entrants must be at least 18, or the older age of majority where they live.",
                    sourceNote = checked(source, "Eligibility item 1"),
                    actionability = Actionability.IMMUTABLE,
                ),
                any(
                    "kotlin-student-status",
                    atomic(
                        id = "kotlin-enrolled",
                        factKey = FactKey.ENROLLED,
                        operator = Operator.EQUALS,
                        expected = bool(true),
                        explanation = "Full-time undergraduate or graduate enrollment satisfies the student-status branch.",
                        sourceNote = checked(source, "Eligibility item 2"),
                        actionability = Actionability.TIME_DEPENDENT,
                        actionTemplateId = enrolledAction.id,
                    ),
                    manual(
                        id = "kotlin-recent-graduate",
                        explanation = "Otherwise confirm full-time enrollment within the 12 months before the contest starts.",
                        sourceNote = checked(source, "Eligibility item 2"),
                    ),
                ),
                manual(
                    id = "kotlin-submission-conditions",
                    explanation = "Confirm sanctions, employment, solo-entry, contest-period, KMP, repository, " +
                        "license, README and essay rules on the live page.",
                    sourceNote = checked(source, "Eligibility and Submission Guidelines"),
                ),
            ),
            softCriteria = listOf(
                "Creativity and novelty",
                "Quality of Kotlin Multiplatform implementation",
                "Alignment with Kotlin coding conventions",
            ),
            readinessItems = listOf(
                "Public GitHub repository with MIT, Apache 2.0 or BSD 3-clause license",
                "Comprehensive README and launch instructions for every declared target",
                "Exactly 300 words of entrant and project background",
                "Recheck sanctions and prize eligibility immediately before submission",
            ),
            actions = listOf(enrolledAction),
        )
    }

    private fun erasmusMundus(): OpportunityDto {
        val source =
            "https://erasmus-plus.ec.europa.eu/opportunities/individuals/students/erasmus-mundus-joint-masters"
        val degreeAction = degreeAction("erasmus-mundus", source)
        return opportunity(
            id = "erasmus-mundus-joint-masters",
            title = "Erasmus Mundus Joint Masters",
            provider = "European Union · Erasmus+",
            category = OpportunityCategory.SCHOLARSHIP,
            cycle = "Programme-specific 2027 intake",
            sourceUrl = source,
            sourceSection = "When to apply; What you need",
            deadline = DeadlineDto.Tba,
            rule = any(
                "erasmus-degree-status",
                atomic(
                    id = "erasmus-degree-complete",
                    factKey = FactKey.DEGREE_COMPLETED,
                    operator = Operator.EQUALS,
                    expected = bool(true),
                    explanation = "Applicants may apply with a completed bachelor's degree.",
                    sourceNote = checked(source, "What you need"),
                    actionability = Actionability.TIME_DEPENDENT,
                    actionTemplateId = degreeAction.id,
                ),
                manual(
                    id = "erasmus-final-year",
                    explanation = "Final-year applicants must confirm they will graduate before their selected master's begins.",
                    sourceNote = checked(source, "What you need"),
                ),
            ),
            softCriteria = listOf("Scholarships are awarded to the best-ranked students."),
            readinessItems = listOf(
                "Choose one consortium programme before relying on its course and language requirements",
                "Confirm the exact programme deadline; the EU page says most applications fall between October and January",
            ),
            actions = listOf(degreeAction),
        )
    }

    private fun chevening(): OpportunityDto {
        val source = "https://www.chevening.org/resource-hub/guidance/eligibility/"
        val degreeAction = degreeAction("chevening", source)
        return opportunity(
            id = "chevening-scholarship-2027",
            title = "Chevening Scholarship",
            provider = "UK Foreign, Commonwealth & Development Office",
            category = OpportunityCategory.SCHOLARSHIP,
            cycle = "Next cycle · dates to be confirmed",
            sourceUrl = source,
            sourceSection = "Chevening eligibility criteria",
            deadline = DeadlineDto.Tba,
            rule = all(
                "chevening-root",
                atomic(
                    id = "chevening-degree",
                    factKey = FactKey.DEGREE_COMPLETED,
                    operator = Operator.EQUALS,
                    expected = bool(true),
                    explanation = "Applicants need an undergraduate degree that qualifies them for a UK master's programme.",
                    sourceNote = checked(source, "Eligibility criteria"),
                    actionability = Actionability.TIME_DEPENDENT,
                    actionTemplateId = degreeAction.id,
                ),
                manual(
                    id = "chevening-experience",
                    explanation = "Confirm at least 2,800 eligible post-graduation work hours under Chevening's counting rules.",
                    sourceNote = checked(source, "Eligibility criteria and work-experience guidance"),
                ),
                manual(
                    id = "chevening-context",
                    explanation = "Confirm eligible citizenship/residency, return commitment, degree timing, " +
                        "employment conflicts and course offers.",
                    sourceNote = checked(source, "Eligibility criteria"),
                ),
            ),
            softCriteria = listOf("Leadership", "Networking", "A clear course and career plan"),
            readinessItems = listOf(
                "Apply to three eligible UK courses",
                "Obtain at least one unconditional offer by the official timeline deadline",
            ),
            actions = listOf(degreeAction),
        )
    }

    private fun rhodes(): OpportunityDto {
        val source = "https://www.rhodeshouse.ox.ac.uk/scholarships/applications/"
        return opportunity(
            id = "rhodes-scholarship",
            title = "Rhodes Scholarship",
            provider = "Rhodes Trust",
            category = OpportunityCategory.SCHOLARSHIP,
            cycle = "Constituency-specific next cycle",
            sourceUrl = source,
            sourceSection = "Check Your Eligibility and Apply",
            deadline = DeadlineDto.Tba,
            rule = manual(
                id = "rhodes-constituency-check",
                explanation = "Age, citizenship/residency, education, nomination and Oxford-course conditions " +
                    "vary by constituency and require the official eligibility checker.",
                sourceNote = checked(source, "Check Your Eligibility and Apply"),
            ),
            softCriteria = listOf(
                "Academic excellence",
                "Energy to use talents fully",
                "Truth, courage, service and leadership",
            ),
            readinessItems = listOf(
                "Select the correct constituency",
                "Verify the constituency deadline and Oxford course requirements",
            ),
        )
    }

    private fun schwarzman(): OpportunityDto {
        val source = "https://www.schwarzmanscholars.org/admissions/application-instructions/"
        val degreeAction = degreeAction("schwarzman", source)
        return opportunity(
            id = "schwarzman-scholars-2028-2029",
            title = "Schwarzman Scholars",
            provider = "Schwarzman Scholars · Tsinghua University",
            category = OpportunityCategory.SCHOLARSHIP,
            cycle = "Class of 2028–2029",
            sourceUrl = source,
            sourceSection = "Next Selection Cycle; Check Your Eligibility",
            deadline = DeadlineDto.Tba,
            rule = all(
                "schwarzman-root",
                atomic(
                    id = "schwarzman-age-min",
                    factKey = FactKey.AGE,
                    operator = Operator.GREATER_THAN_OR_EQUAL,
                    expected = number(18.0, "years"),
                    explanation = "Candidates must be at least 18 as of 1 August in the enrollment year.",
                    sourceNote = checked(source, "Check Your Eligibility"),
                    actionability = Actionability.IMMUTABLE,
                ),
                atomic(
                    id = "schwarzman-age-max",
                    factKey = FactKey.AGE,
                    operator = Operator.LESS_THAN_OR_EQUAL,
                    expected = number(28.0, "years"),
                    explanation = "Candidates must not yet be 29 as of 1 August in the enrollment year.",
                    sourceNote = checked(source, "Check Your Eligibility"),
                    actionability = Actionability.IMMUTABLE,
                ),
                atomic(
                    id = "schwarzman-degree",
                    factKey = FactKey.DEGREE_COMPLETED,
                    operator = Operator.EQUALS,
                    expected = bool(true),
                    explanation = "All undergraduate degree requirements must be complete before 1 August 2028.",
                    sourceNote = checked(source, "Check Your Eligibility"),
                    actionability = Actionability.TIME_DEPENDENT,
                    actionTemplateId = degreeAction.id,
                ),
                manual(
                    id = "schwarzman-english",
                    explanation = "Confirm an accepted English-proficiency report or an official exemption " +
                        "based on prior English-medium study.",
                    sourceNote = checked(source, "English language requirements"),
                ),
            ),
            softCriteria = listOf("Leadership", "Intellectual ability", "Character and integrity"),
            readinessItems = listOf("Confirm the exact 2027 application close date when published"),
            actions = listOf(degreeAction),
        )
    }

    private fun eiffel(): OpportunityDto {
        val source = "https://www.campusfrance.org/en/the-france-excellence-eiffel-scholarship-program"
        return opportunity(
            id = "france-excellence-eiffel-2027",
            title = "France Excellence Eiffel Scholarship",
            provider = "French Ministry for Europe and Foreign Affairs · Campus France",
            category = OpportunityCategory.SCHOLARSHIP,
            cycle = "2027 campaign",
            sourceUrl = source,
            sourceSection = "2027 call and regulations",
            deadline = DeadlineDto.Known("2027-01-08"),
            rule = manual(
                id = "eiffel-official-check",
                explanation = "Confirm foreign-national status, age band, eligible study field and nomination " +
                    "by a French institution under the 2027 regulations.",
                sourceNote = checked(source, "2027 regulations and application process"),
            ),
            softCriteria = listOf("Academic excellence", "International profile"),
            readinessItems = listOf(
                "Students cannot submit directly; a French higher-education institution must nominate",
                "Ask the institution for its earlier internal deadline",
            ),
        )
    }

    private fun daadEpos(): OpportunityDto {
        val source =
            "https://www2.daad.de/deutschland/stipendium/datenbank/en/21148-scholarship-database.pdf?detail=50076777"
        val degreeAction = degreeAction("daad-epos", source)
        val experienceAction = experienceAction("daad-epos", 24.0, source)
        return opportunity(
            id = "daad-epos-2027-2028",
            title = "DAAD EPOS",
            provider = "German Academic Exchange Service (DAAD)",
            category = OpportunityCategory.SCHOLARSHIP,
            cycle = "2027–2028 intake · course-specific",
            sourceUrl = source,
            sourceSection = "Requirements; EPOS course list",
            deadline = DeadlineDto.Tba,
            rule = all(
                "daad-root",
                atomic(
                    id = "daad-degree",
                    factKey = FactKey.DEGREE_COMPLETED,
                    operator = Operator.EQUALS,
                    expected = bool(true),
                    explanation = "Applicants need an appropriate first university degree.",
                    sourceNote = checked(source, "Requirements"),
                    actionability = Actionability.TIME_DEPENDENT,
                    actionTemplateId = degreeAction.id,
                ),
                atomic(
                    id = "daad-experience-duration",
                    factKey = FactKey.EXPERIENCE_MONTHS,
                    operator = Operator.GREATER_THAN_OR_EQUAL,
                    expected = number(24.0, "months"),
                    explanation = "The general EPOS criteria require at least two years of professional experience.",
                    sourceNote = checked(source, "Requirements"),
                    actionability = Actionability.TIME_DEPENDENT,
                    actionTemplateId = experienceAction.id,
                ),
                manual(
                    id = "daad-experience-relevance",
                    explanation = "Confirm that the experience is relevant, post-degree where required, and " +
                        "accepted by the selected EPOS course.",
                    sourceNote = checked(source, "Requirements and selected course page"),
                ),
                manual(
                    id = "daad-country-course",
                    explanation = "Confirm developing-country scope, course fit, language requirements and the course-specific deadline.",
                    sourceNote = checked(source, "EPOS country and course lists"),
                ),
            ),
            softCriteria = listOf("Above-average academic performance", "Development motivation"),
            readinessItems = listOf("Apply to the selected university/course, not directly to DAAD unless instructed"),
            actions = listOf(degreeAction, experienceAction),
        )
    }

    private fun jjWbgsp(): OpportunityDto {
        val source = "https://www.worldbank.org/en/programs/scholarships/jj-wbgsp"
        val degreeAction = degreeAction("jj-wbgsp", source)
        val experienceAction = experienceAction("jj-wbgsp", 36.0, source)
        return opportunity(
            id = "jj-wbgsp-2027-window-1",
            title = "Joint Japan/World Bank Graduate Scholarship",
            provider = "World Bank",
            category = OpportunityCategory.SCHOLARSHIP,
            cycle = "2027 application window 1",
            sourceUrl = source,
            sourceSection = "2027 Application Cycle; Who is Eligible to Apply?",
            deadline = DeadlineDto.Known("2027-02-26"),
            rule = all(
                "jj-root",
                atomic(
                    id = "jj-degree",
                    factKey = FactKey.DEGREE_COMPLETED,
                    operator = Operator.EQUALS,
                    expected = bool(true),
                    explanation = "Applicants must hold a bachelor's degree or equivalent.",
                    sourceNote = checked(source, "Who is Eligible to Apply?"),
                    actionability = Actionability.TIME_DEPENDENT,
                    actionTemplateId = degreeAction.id,
                ),
                atomic(
                    id = "jj-experience-duration",
                    factKey = FactKey.EXPERIENCE_MONTHS,
                    operator = Operator.GREATER_THAN_OR_EQUAL,
                    expected = number(36.0, "months"),
                    explanation = "The broad criteria require at least three years of paid development-related employment.",
                    sourceNote = checked(source, "Who is Eligible to Apply?"),
                    actionability = Actionability.TIME_DEPENDENT,
                    actionTemplateId = experienceAction.id,
                ),
                manual(
                    id = "jj-experience-timing",
                    explanation = "Confirm the work is eligible, paid, development-related, post-degree and " +
                        "within the official six-year counting window.",
                    sourceNote = checked(source, "Who is Eligible to Apply? and application guidelines"),
                ),
                manual(
                    id = "jj-country-admission",
                    explanation = "Confirm eligible nationality, no disqualifying dual citizenship, and " +
                        "unconditional admission to a participating programme.",
                    sourceNote = checked(source, "2027 Application Cycle"),
                ),
            ),
            softCriteria = listOf("Commitment to supporting the applicant's country's development"),
            readinessItems = listOf("The university must shortlist the admitted candidate before the application link is issued"),
            actions = listOf(degreeAction, experienceAction),
        )
    }

    private fun oecdInternship(): OpportunityDto {
        val source = "https://www.oecd.org/en/about/careers/internships.html"
        val enrollmentAction = enrollmentAction("oecd", source)
        val availabilityAction = availabilityAction("oecd", "oecd-availability", source)
        return opportunity(
            id = "oecd-internship-rolling",
            title = "OECD Internship Programme",
            provider = "Organisation for Economic Co-operation and Development",
            category = OpportunityCategory.INTERNSHIP,
            cycle = "Rolling applications",
            sourceUrl = source,
            sourceSection = "Who can apply?; Internship duration",
            deadline = DeadlineDto.Missing,
            rule = all(
                "oecd-root",
                atomic(
                    id = "oecd-enrolled",
                    factKey = FactKey.ENROLLED,
                    operator = Operator.EQUALS,
                    expected = bool(true),
                    explanation = "Applicants must remain enrolled in a full-time accredited degree for the internship.",
                    sourceNote = checked(source, "Who can apply?"),
                    actionability = Actionability.TIME_DEPENDENT,
                    actionTemplateId = enrollmentAction.id,
                ),
                atomic(
                    id = "oecd-availability",
                    factKey = FactKey.AVAILABLE_FULL_PERIOD,
                    operator = Operator.EQUALS,
                    expected = bool(true),
                    explanation = "Applicants must be available for at least one month.",
                    sourceNote = checked(source, "Who can apply? and Internship duration"),
                    actionability = Actionability.CONTROLLABLE,
                    actionTemplateId = availabilityAction.id,
                ),
                manual(
                    id = "oecd-field-language",
                    explanation = "Confirm a relevant study field and the official-language expectations for the intended team.",
                    sourceNote = checked(source, "Who can apply?"),
                ),
            ),
            softCriteria = listOf("Quantitative and IT skills", "Drafting and communication skills"),
            readinessItems = listOf("State accurate availability; OECD recruits on a rolling basis"),
            actions = listOf(enrollmentAction, availabilityAction),
        )
    }

    private fun unicefInternship(): OpportunityDto {
        val source = "https://www.unicef.org/careers/internships"
        val enrollmentAction = enrollmentAction("unicef", source)
        return opportunity(
            id = "unicef-internship-rolling",
            title = "UNICEF Internship Programme",
            provider = "UNICEF",
            category = OpportunityCategory.INTERNSHIP,
            cycle = "Vacancies throughout the year",
            sourceUrl = source,
            sourceSection = "Requirements; How to apply",
            deadline = DeadlineDto.Missing,
            rule = all(
                "unicef-root",
                atomic(
                    id = "unicef-age",
                    factKey = FactKey.AGE,
                    operator = Operator.GREATER_THAN_OR_EQUAL,
                    expected = number(18.0, "years"),
                    explanation = "Applicants must be at least 18 years old.",
                    sourceNote = checked(source, "Requirements"),
                    actionability = Actionability.IMMUTABLE,
                ),
                any(
                    "unicef-study-status",
                    atomic(
                        id = "unicef-enrolled",
                        factKey = FactKey.ENROLLED,
                        operator = Operator.EQUALS,
                        expected = bool(true),
                        explanation = "Current undergraduate, graduate or PhD enrollment satisfies the study-status branch.",
                        sourceNote = checked(source, "Requirements"),
                        actionability = Actionability.TIME_DEPENDENT,
                        actionTemplateId = enrollmentAction.id,
                    ),
                    manual(
                        id = "unicef-recent-graduate",
                        explanation = "Otherwise confirm graduation within the past two years.",
                        sourceNote = checked(source, "Requirements"),
                    ),
                ),
                manual(
                    id = "unicef-language-family",
                    explanation = "Confirm the vacancy language requirement and UNICEF family/reporting-line conflict rules.",
                    sourceNote = checked(source, "Requirements"),
                ),
            ),
            softCriteria = listOf("Strong academic performance", "Motivation", "Relevant professional experience"),
            readinessItems = listOf("Review the requirements and deadline of the specific vacancy"),
            actions = listOf(enrollmentAction),
        )
    }

    private fun worldBankTreasury(): OpportunityDto {
        val source = "https://treasury.worldbank.org/en/about/unit/treasury/about/student-and-graduate-careers"
        val enrollmentAction = enrollmentAction("world-bank-treasury", source)
        val availabilityAction = availabilityAction(
            "world-bank-treasury",
            "world-bank-treasury-availability",
            source,
        )
        return opportunity(
            id = "world-bank-treasury-summer-internship-2027",
            title = "World Bank Treasury Summer Internship",
            provider = "World Bank Treasury",
            category = OpportunityCategory.INTERNSHIP,
            cycle = "2027 summer internship",
            sourceUrl = source,
            sourceSection = "Internship Calendar; Eligibility",
            deadline = DeadlineDto.Tba,
            rule = all(
                "world-bank-treasury-root",
                atomic(
                    id = "world-bank-treasury-enrolled",
                    factKey = FactKey.ENROLLED,
                    operator = Operator.EQUALS,
                    expected = bool(true),
                    explanation = "Applicants must be enrolled in the required stage of a four-year-equivalent degree.",
                    sourceNote = checked(source, "Eligibility"),
                    actionability = Actionability.TIME_DEPENDENT,
                    actionTemplateId = enrollmentAction.id,
                ),
                atomic(
                    id = "world-bank-treasury-availability",
                    factKey = FactKey.AVAILABLE_FULL_PERIOD,
                    operator = Operator.EQUALS,
                    expected = bool(true),
                    explanation = "Applicants must be available full-time for the entire internship.",
                    sourceNote = checked(source, "Eligibility and Internship Calendar"),
                    actionability = Actionability.CONTROLLABLE,
                    actionTemplateId = availabilityAction.id,
                ),
                manual(
                    id = "world-bank-treasury-stage",
                    explanation = "Confirm the exact graduation window, good standing, relevant field and " +
                        "absence of concurrent work or internship.",
                    sourceNote = checked(source, "Eligibility"),
                ),
            ),
            softCriteria = listOf("Interest in finance, business or economics", "Finance and technology strengths"),
            readinessItems = listOf("Confirm the application close date on the live page"),
            actions = listOf(enrollmentAction, availabilityAction),
        )
    }

    private fun esaInternship(): OpportunityDto {
        val source = "https://www.esa.int/About_Us/Careers_at_ESA/Student_Internships2"
        val enrollmentAction = enrollmentAction("esa", source)
        val availabilityAction = availabilityAction("esa", "esa-availability", source)
        return opportunity(
            id = "esa-student-internships",
            title = "ESA Student Internships",
            provider = "European Space Agency",
            category = OpportunityCategory.INTERNSHIP,
            cycle = "Vacancy-specific next cycle",
            sourceUrl = source,
            sourceSection = "Who can apply?",
            deadline = DeadlineDto.Tba,
            rule = all(
                "esa-root",
                atomic(
                    id = "esa-enrolled",
                    factKey = FactKey.ENROLLED,
                    operator = Operator.EQUALS,
                    expected = bool(true),
                    explanation = "Applicants must retain university student status for the full internship.",
                    sourceNote = checked(source, "Who can apply?"),
                    actionability = Actionability.TIME_DEPENDENT,
                    actionTemplateId = enrollmentAction.id,
                ),
                atomic(
                    id = "esa-availability",
                    factKey = FactKey.AVAILABLE_FULL_PERIOD,
                    operator = Operator.EQUALS,
                    expected = bool(true),
                    explanation = "The selected placement lasts between three and six months.",
                    sourceNote = checked(source, "Who can apply? and placement duration"),
                    actionability = Actionability.CONTROLLABLE,
                    actionTemplateId = availabilityAction.id,
                ),
                manual(
                    id = "esa-citizenship-vacancy",
                    explanation = "Confirm eligible citizenship and every requirement in the selected ESA vacancy.",
                    sourceNote = checked(source, "Who can apply?"),
                ),
            ),
            softCriteria = listOf("Final or second-to-last year of a master's course is preferred"),
            readinessItems = listOf("Use the citizenship list and deadline in the selected vacancy"),
            actions = listOf(enrollmentAction, availabilityAction),
        )
    }

    private fun cernOpenlab(): OpportunityDto {
        val source = "https://openlab.web.cern.ch/cern-openlab-summer-student-programme/"
        val enrollmentAction = enrollmentAction("cern-openlab", source)
        val availabilityAction = availabilityAction("cern-openlab", "cern-openlab-availability", source)
        return opportunity(
            id = "cern-openlab-summer-student-2027",
            title = "CERN openlab Summer Student Programme",
            provider = "CERN openlab",
            category = OpportunityCategory.ACADEMIC_PROGRAM,
            cycle = "Summer 2027",
            sourceUrl = source,
            sourceSection = "Who can apply?; How and when to apply?",
            deadline = DeadlineDto.Tba,
            rule = all(
                "cern-openlab-root",
                atomic(
                    id = "cern-openlab-enrolled",
                    factKey = FactKey.ENROLLED,
                    operator = Operator.EQUALS,
                    expected = bool(true),
                    explanation = "The programme is for BSc or MSc students.",
                    sourceNote = checked(source, "Who can apply?"),
                    actionability = Actionability.TIME_DEPENDENT,
                    actionTemplateId = enrollmentAction.id,
                ),
                atomic(
                    id = "cern-openlab-availability",
                    factKey = FactKey.AVAILABLE_FULL_PERIOD,
                    operator = Operator.EQUALS,
                    expected = bool(true),
                    explanation = "Participants must be available for the nine-week June–August programme.",
                    sourceNote = checked(source, "Who can apply? and programme duration"),
                    actionability = Actionability.CONTROLLABLE,
                    actionTemplateId = availabilityAction.id,
                ),
                manual(
                    id = "cern-openlab-study-profile",
                    explanation = "Confirm three years of full-time university study by June, an accepted field " +
                        "and the programme's computing-profile requirement.",
                    sourceNote = checked(source, "Who can apply?"),
                ),
            ),
            softCriteria = listOf("Strong computing profile"),
            readinessItems = listOf(
                "Prepare a CV, official transcript and academic reference letter",
                "The official page says the 2027 call opens in November 2026; confirm the closing date then",
            ),
            actions = listOf(enrollmentAction, availabilityAction),
        )
    }

    private fun googleSummerOfCode(): OpportunityDto {
        val source = "https://developers.google.com/open-source/gsoc/faq"
        return opportunity(
            id = "google-summer-of-code-next-cycle",
            title = "Google Summer of Code",
            provider = "Google Open Source Programs Office",
            category = OpportunityCategory.ACADEMIC_PROGRAM,
            cycle = "Next contributor cycle · timeline to be confirmed",
            sourceUrl = source,
            sourceSection = "Eligibility; GSoC Contributor Applicants",
            deadline = DeadlineDto.Tba,
            rule = all(
                "gsoc-root",
                atomic(
                    id = "gsoc-age",
                    factKey = FactKey.AGE,
                    operator = Operator.GREATER_THAN_OR_EQUAL,
                    expected = number(18.0, "years"),
                    explanation = "Contributors must be at least 18 at registration.",
                    sourceNote = checked(source, "Eligibility"),
                    actionability = Actionability.IMMUTABLE,
                ),
                manual(
                    id = "gsoc-context",
                    explanation = "Confirm work eligibility, non-embargoed residence, beginner status, prior " +
                        "acceptance count and no prior mentor/admin conflict.",
                    sourceNote = checked(source, "Eligibility"),
                ),
            ),
            softCriteria = listOf("Early mentor interaction", "Detailed goals, methodology and timeline"),
            readinessItems = listOf(
                "Choose a participating organization and follow its language and AI-use policies",
                "Confirm the contributor application window on the current timeline",
            ),
        )
    }

    private fun swiftStudentChallenge(): OpportunityDto {
        val source = "https://developer.apple.com/swift-student-challenge/policy/"
        val enrollmentAction = enrollmentAction("swift-student-challenge", source)
        return opportunity(
            id = "swift-student-challenge-2026",
            title = "Swift Student Challenge",
            provider = "Apple",
            category = OpportunityCategory.STUDENT_CONTEST,
            cycle = "2026 terms · archived",
            sourceUrl = source,
            sourceSection = "Deadlines; Application Eligibility Requirements; Submission Requirements",
            deadline = DeadlineDto.Known("2026-02-28"),
            rule = all(
                "swift-challenge-root",
                any(
                    "swift-challenge-education-status",
                    atomic(
                        id = "swift-challenge-enrolled",
                        factKey = FactKey.ENROLLED,
                        operator = Operator.EQUALS,
                        expected = bool(true),
                        explanation = "Current enrollment in an accepted education path satisfies one education-status branch.",
                        sourceNote = checked(source, "Application Eligibility Requirements"),
                        actionability = Actionability.TIME_DEPENDENT,
                        actionTemplateId = enrollmentAction.id,
                    ),
                    manual(
                        id = "swift-challenge-other-education",
                        explanation = "Otherwise confirm one of Apple's recent-graduate, homeschool, academy or " +
                            "STEM-organization branches.",
                        sourceNote = checked(source, "Application Eligibility Requirements"),
                    ),
                ),
                manual(
                    id = "swift-challenge-age-context",
                    explanation = "Confirm the country-specific minimum age, developer registration, employment " +
                        "exclusions and award-history limits.",
                    sourceNote = checked(source, "Application Eligibility Requirements"),
                ),
                manual(
                    id = "swift-challenge-artifact",
                    explanation = "Confirm the solo offline Swift Playground app and all current artifact and " +
                        "proof-of-enrollment requirements.",
                    sourceNote = checked(source, "Submission Requirements"),
                ),
            ),
            softCriteria = listOf("Technical accomplishment", "Creativity", "Written explanation"),
            readinessItems = listOf("This archived record must not be reused for a future challenge without new terms"),
            actions = listOf(enrollmentAction),
            archived = true,
        )
    }

    private fun opportunity(
        id: String,
        title: String,
        provider: String,
        category: OpportunityCategory,
        cycle: String,
        sourceUrl: String,
        sourceSection: String,
        deadline: DeadlineDto,
        rule: RuleNodeDto,
        softCriteria: List<String> = emptyList(),
        readinessItems: List<String> = emptyList(),
        actions: List<ActionTemplateDto> = emptyList(),
        archived: Boolean = false,
    ): OpportunityDto = OpportunityDto(
        id = id,
        title = title,
        provider = provider,
        category = category.name,
        cycle = cycle,
        sourceUrl = sourceUrl,
        sourceSection = sourceSection,
        lastCheckedOn = VERIFIED_ON,
        deadline = deadline,
        eligibilityRule = rule,
        softCriteria = softCriteria,
        readinessItems = readinessItems,
        actionTemplates = actions,
        disclaimer = Opportunity.DEFAULT_DISCLAIMER,
        archived = archived,
    )

    private fun all(id: String, vararg children: RuleNodeDto): RuleNodeDto = RuleNodeDto.All(id = id, children = children.toList())

    private fun any(id: String, vararg children: RuleNodeDto): RuleNodeDto = RuleNodeDto.Any(id = id, children = children.toList())

    private fun atomic(
        id: String,
        factKey: FactKey,
        operator: Operator,
        expected: FactValueDto,
        explanation: String,
        sourceNote: String,
        actionability: Actionability,
        actionTemplateId: String? = null,
    ): RuleNodeDto = RuleNodeDto.Atomic(
        id = id,
        factKey = factKey.name,
        operator = operator.name,
        expected = expected,
        explanation = explanation,
        sourceNote = sourceNote,
        actionability = actionability.name,
        actionTemplateId = actionTemplateId,
    )

    private fun manual(
        id: String,
        explanation: String,
        sourceNote: String,
    ): RuleNodeDto = RuleNodeDto.ManualCheck(
        id = id,
        explanation = explanation,
        sourceNote = sourceNote,
    )

    private fun degreeAction(id: String, source: String): ActionTemplateDto = action(
        id = "$id-complete-degree",
        title = "Complete the required first degree",
        targetFactKey = FactKey.DEGREE_COMPLETED,
        targetValue = bool(true),
        targetActionability = Actionability.TIME_DEPENDENT,
        minimumDurationDays = 180,
        effort = EffortLevel.HIGH,
        costBand = CostBand.MEDIUM,
        evidence = setOf("degree certificate", "official completion letter"),
        sourceNote = planningNote(source),
    )

    private fun enrollmentAction(id: String, source: String): ActionTemplateDto = action(
        id = "$id-valid-enrollment",
        title = "Use a cycle while eligible enrollment is active",
        targetFactKey = FactKey.ENROLLED,
        targetValue = bool(true),
        targetActionability = Actionability.TIME_DEPENDENT,
        minimumDurationDays = 30,
        effort = EffortLevel.LOW,
        costBand = CostBand.FREE,
        evidence = setOf("current enrollment letter"),
        sourceNote = planningNote(source),
    )

    private fun availabilityAction(
        id: String,
        targetRuleId: String,
        source: String,
    ): ActionTemplateDto = action(
        id = "$id-full-period-availability",
        title = "Reserve the full required programme period",
        targetFactKey = FactKey.AVAILABLE_FULL_PERIOD,
        targetValue = bool(true),
        targetScope = ActionTargetScope.RULE,
        targetRuleId = targetRuleId,
        targetActionability = Actionability.CONTROLLABLE,
        minimumDurationDays = 14,
        effort = EffortLevel.LOW,
        costBand = CostBand.FREE,
        evidence = setOf("calendar plan", "study or employer approval"),
        sourceNote = planningNote(source),
    )

    private fun experienceAction(
        id: String,
        months: Double,
        source: String,
    ): ActionTemplateDto = action(
        id = "$id-experience-${months.toInt()}-months",
        title = "Reach ${months.toInt()} months of relevant experience",
        targetFactKey = FactKey.EXPERIENCE_MONTHS,
        targetValue = number(months, "months"),
        targetActionability = Actionability.TIME_DEPENDENT,
        minimumDurationDays = (months * 30).toInt(),
        effort = EffortLevel.VERY_HIGH,
        costBand = CostBand.FREE,
        evidence = setOf("employment letters", "contracts or payslips"),
        sourceNote = planningNote(source),
    )

    private fun action(
        id: String,
        title: String,
        targetFactKey: FactKey,
        targetValue: FactValueDto,
        targetScope: ActionTargetScope = ActionTargetScope.PROFILE,
        targetRuleId: String? = null,
        targetActionability: Actionability,
        minimumDurationDays: Int,
        effort: EffortLevel,
        costBand: CostBand,
        evidence: Set<String>,
        sourceNote: String,
    ): ActionTemplateDto = ActionTemplateDto(
        id = id,
        title = title,
        targetFactKey = targetFactKey.name,
        targetValue = targetValue,
        targetScope = targetScope.name,
        targetRuleId = targetRuleId,
        targetActionability = targetActionability.name,
        minimumDurationDays = minimumDurationDays,
        effort = effort.name,
        costBand = costBand.name,
        completionEvidenceTypes = evidence,
        sourceNote = sourceNote,
    )

    private fun bool(value: Boolean): FactValueDto = FactValueDto.BooleanValue(value)

    private fun number(amount: Double, unit: String): FactValueDto = FactValueDto.NumberValue(amount, unit)

    private fun checked(source: String, section: String): String = "$section on the official provider page ($source), checked $VERIFIED_ON."

    private fun planningNote(
        source: String,
    ): String = "The official source establishes the criterion ($source). " +
        "Duration, effort and cost are transparent Porta planning estimates."
}
