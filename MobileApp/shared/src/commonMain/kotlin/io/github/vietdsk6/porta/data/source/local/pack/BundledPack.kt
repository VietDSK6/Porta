package io.github.vietdsk6.porta.data.source.local.pack

import io.github.vietdsk6.porta.domain.dsl.PackBuilder
import io.github.vietdsk6.porta.domain.dsl.opportunityPack
import io.github.vietdsk6.porta.domain.model.ActionTarget
import io.github.vietdsk6.porta.domain.model.ActionTemplate
import io.github.vietdsk6.porta.domain.model.Actionability
import io.github.vietdsk6.porta.domain.model.CostBand
import io.github.vietdsk6.porta.domain.model.EffortLevel
import io.github.vietdsk6.porta.domain.model.FactKey
import io.github.vietdsk6.porta.domain.model.FactValue
import io.github.vietdsk6.porta.domain.model.Operator
import io.github.vietdsk6.porta.domain.model.OpportunityCategory
import io.github.vietdsk6.porta.domain.model.OpportunityPackDocument
import io.github.vietdsk6.porta.domain.model.RuleNode
import io.github.vietdsk6.porta.domain.model.findAtomicRule
import io.github.vietdsk6.porta.domain.model.fingerprint
import kotlinx.datetime.LocalDate

/**
 * The offline catalog shipped with Porta.
 *
 * Every record is independently curated from the linked provider page. A manual rule is deliberate:
 * it means the official condition is real, but the current typed profile cannot evaluate it without
 * inventing an equivalence. Estimates on action templates are Porta planning aids, not provider data.
 */
internal object BundledPack {
    const val REVIEWED_ON = "2026-10-21"
    const val SOURCE_REFRESH_REVIEWED_ON = "2026-11-02"
    const val CERN_REVIEWED_ON = "2026-11-14"
    const val EIFFEL_REVIEWED_ON = "2026-11-14"

    /** Curated provider records declared directly through Porta's typed pack DSL. */
    fun document(): OpportunityPackDocument = opportunityPack(
        id = BundledOpportunityPackProvider.PACK_ID,
        name = "Porta curated opportunity pack",
        publisher = "Porta · independently curated from official sources",
        lastCheckedOn = LocalDate.parse(REVIEWED_ON),
        version = BundledOpportunityPackProvider.PACK_VERSION,
        schemaVersion = OpportunityPackCodec.CURRENT_SCHEMA_VERSION,
    ) {
        kotlinStudentContest()
        erasmusMundus()
        chevening()
        rhodes()
        schwarzman()
        eiffel()
        daadEpos()
        jjWbgsp()
        oecdInternship()
        unicefInternship()
        worldBankTreasury()
        esaInternship()
        cernOpenlab()
        googleSummerOfCode()
        swiftStudentChallenge()
    }

    fun create(): OpportunityPackDto = document().toDto()

    private fun PackBuilder.kotlinStudentContest() {
        val source = "https://kotlinconf.com/contest-rules/"
        val enrolledAction = enrollmentAction("kotlin-contest", source)
        opportunity(
            id = "kotlin-student-coding-competition-2026",
            title = "Kotlin Student Coding Competition",
            provider = "Kotlin Foundation",
            category = OpportunityCategory.STUDENT_CONTEST,
            cycle = "2026 competition",
        ) {
            source(source, "Eligibility; How to Enter", LocalDate.parse(REVIEWED_ON))
            deadline { known("2026-12-07") }
            eligibility {
                all("kotlin-root") {
                    atomic(
                        id = "kotlin-age",
                        factKey = FactKey.AGE,
                        operator = Operator.GREATER_THAN_OR_EQUAL,
                        expected = number(18.0, "years"),
                        explanation = "Entrants must be at least 18, or the older age of majority where they live.",
                        sourceNote = checked(source, "Eligibility item 1"),
                        actionability = Actionability.IMMUTABLE,
                    )
                    any("kotlin-student-status") {
                        atomic(
                            id = "kotlin-enrolled",
                            factKey = FactKey.ENROLLED,
                            operator = Operator.EQUALS,
                            expected = bool(true),
                            explanation = "Full-time undergraduate or graduate enrollment satisfies the student-status branch.",
                            sourceNote = checked(source, "Eligibility item 2"),
                            actionability = Actionability.TIME_DEPENDENT,
                            actionTemplateId = enrolledAction.id,
                        )
                        manual(
                            id = "kotlin-recent-graduate",
                            explanation = "Otherwise confirm full-time enrollment within the 12 months before the contest starts.",
                            sourceNote = checked(source, "Eligibility item 2"),
                        )
                    }
                    manual(
                        id = "kotlin-submission-conditions",
                        explanation = "Confirm sanctions, employment, solo-entry, contest-period, KMP, repository, " +
                            "license, README and essay rules on the live page.",
                        sourceNote = checked(source, "Eligibility and Submission Guidelines"),
                    )
                }
            }
            softCriteria.addAll(
                listOf(
                    "Creativity and novelty",
                    "Quality of Kotlin Multiplatform implementation",
                    "Alignment with Kotlin coding conventions",
                ),
            )
            readinessItems.addAll(
                listOf(
                    "Public GitHub repository with MIT, Apache 2.0 or BSD 3-clause license",
                    "Comprehensive README and launch instructions for every declared target",
                    "Exactly 300 words of entrant and project background",
                    "Recheck sanctions and prize eligibility immediately before submission",
                ),
            )
            action(enrolledAction)
        }
    }

    private fun PackBuilder.erasmusMundus() {
        val source =
            "https://erasmus-plus.ec.europa.eu/opportunities/individuals/students/erasmus-mundus-joint-masters"
        val degreeAction = degreeAction("erasmus-mundus", source)
        opportunity(
            id = "erasmus-mundus-joint-masters",
            title = "Erasmus Mundus Joint Masters",
            provider = "European Union · Erasmus+",
            category = OpportunityCategory.SCHOLARSHIP,
            cycle = "Programme-specific 2027 intake",
        ) {
            source(source, "When to apply; What you need", LocalDate.parse(SOURCE_REFRESH_REVIEWED_ON))
            deadline { tba() }
            eligibility {
                any("erasmus-degree-status") {
                    atomic(
                        id = "erasmus-degree-complete",
                        factKey = FactKey.DEGREE_COMPLETED,
                        operator = Operator.EQUALS,
                        expected = bool(true),
                        explanation = "Applicants may apply with a completed bachelor's degree.",
                        sourceNote = checked(source, "What you need", SOURCE_REFRESH_REVIEWED_ON),
                        actionability = Actionability.TIME_DEPENDENT,
                        actionTemplateId = degreeAction.id,
                    )
                    manual(
                        id = "erasmus-final-year",
                        explanation = "Final-year applicants must confirm they will graduate before their selected master's begins.",
                        sourceNote = checked(source, "What you need", SOURCE_REFRESH_REVIEWED_ON),
                    )
                }
            }
            softCriteria.addAll(listOf("Scholarships are awarded to the best-ranked students."))
            readinessItems.addAll(
                listOf(
                    "Choose one consortium programme before relying on its course and language requirements",
                    "Confirm the exact programme deadline; the EU page says most applications fall between October and January",
                ),
            )
            action(degreeAction)
        }
    }

    private fun PackBuilder.chevening() {
        val eligibilitySource = "https://www.chevening.org/resource-hub/guidance/eligibility/"
        val timelineSource = "https://www.chevening.org/scholarships/application-timeline/"
        val degreeAction = degreeAction("chevening", eligibilitySource)
        opportunity(
            id = "chevening-scholarship-2027",
            title = "Chevening Scholarship",
            provider = "UK Foreign, Commonwealth & Development Office",
            category = OpportunityCategory.SCHOLARSHIP,
            cycle = "2027–28 awards · applications closed October 2026",
        ) {
            source(timelineSource, "Chevening eligibility criteria; Application timeline", LocalDate.parse(SOURCE_REFRESH_REVIEWED_ON))
            deadline { tba() }
            eligibility {
                all("chevening-root") {
                    atomic(
                        id = "chevening-degree",
                        factKey = FactKey.DEGREE_COMPLETED,
                        operator = Operator.EQUALS,
                        expected = bool(true),
                        explanation = "Applicants need an undergraduate degree that qualifies them for a UK master's programme.",
                        sourceNote = checked(eligibilitySource, "Eligibility criteria", SOURCE_REFRESH_REVIEWED_ON),
                        actionability = Actionability.TIME_DEPENDENT,
                        actionTemplateId = degreeAction.id,
                    )
                    manual(
                        id = "chevening-experience",
                        explanation = "Confirm at least 2,800 eligible post-graduation work hours under Chevening's counting rules.",
                        sourceNote = checked(
                            eligibilitySource,
                            "Eligibility criteria and work-experience guidance",
                            SOURCE_REFRESH_REVIEWED_ON,
                        ),
                    )
                    manual(
                        id = "chevening-context",
                        explanation = "Confirm eligible citizenship/residency, return commitment, degree timing, " +
                            "employment conflicts and course offers.",
                        sourceNote = checked(eligibilitySource, "Eligibility criteria", SOURCE_REFRESH_REVIEWED_ON),
                    )
                }
            }
            softCriteria.addAll(listOf("Leadership", "Networking", "A clear course and career plan"))
            readinessItems.addAll(
                listOf(
                    "Apply to three eligible UK courses",
                    "Obtain at least one unconditional offer by the official timeline deadline",
                    "Applications for 2027–28 closed in October 2026; recheck the next published cycle on $timelineSource",
                ),
            )
            action(degreeAction)
            archived = true
        }
    }

    private fun PackBuilder.rhodes() {
        val source = "https://www.rhodeshouse.ox.ac.uk/scholarships/applications/"
        opportunity(
            id = "rhodes-scholarship",
            title = "Rhodes Scholarship",
            provider = "Rhodes Trust",
            category = OpportunityCategory.SCHOLARSHIP,
            cycle = "Constituency-specific next cycle",
        ) {
            source(source, "Check Your Eligibility and Apply", LocalDate.parse(REVIEWED_ON))
            deadline { tba() }
            eligibility {
                manual(
                    id = "rhodes-constituency-check",
                    explanation = "Age, citizenship/residency, education, nomination and Oxford-course conditions " +
                        "vary by constituency and require the official eligibility checker.",
                    sourceNote = checked(source, "Check Your Eligibility and Apply"),
                )
            }
            softCriteria.addAll(
                listOf(
                    "Academic excellence",
                    "Energy to use talents fully",
                    "Truth, courage, service and leadership",
                ),
            )
            readinessItems.addAll(
                listOf(
                    "Select the correct constituency",
                    "Verify the constituency deadline and Oxford course requirements",
                ),
            )
        }
    }

    private fun PackBuilder.schwarzman() {
        val source = "https://www.schwarzmanscholars.org/admissions/application-instructions/"
        val degreeAction = degreeAction("schwarzman", source)
        val ieltsAction = ieltsAction("schwarzman", 7.0, source)
        opportunity(
            id = "schwarzman-scholars-2028-2029",
            title = "Schwarzman Scholars",
            provider = "Schwarzman Scholars · Tsinghua University",
            category = OpportunityCategory.SCHOLARSHIP,
            cycle = "Class of 2028–2029",
        ) {
            source(source, "Next Selection Cycle; Check Your Eligibility; Language Skills", LocalDate.parse(SOURCE_REFRESH_REVIEWED_ON))
            deadline { tba() }
            eligibility {
                all("schwarzman-root") {
                    atomic(
                        id = "schwarzman-age-min",
                        factKey = FactKey.DATE_OF_BIRTH,
                        operator = Operator.DATE_BEFORE,
                        expected = date("2010-08-02"),
                        explanation = "Candidates must be at least 18 as of 1 August in the enrollment year.",
                        sourceNote = checked(source, "Check Your Eligibility", SOURCE_REFRESH_REVIEWED_ON),
                        actionability = Actionability.IMMUTABLE,
                    )
                    atomic(
                        id = "schwarzman-age-max",
                        factKey = FactKey.DATE_OF_BIRTH,
                        operator = Operator.DATE_AFTER,
                        expected = date("1999-08-01"),
                        explanation = "Candidates must not yet be 29 as of 1 August in the enrollment year.",
                        sourceNote = checked(source, "Check Your Eligibility", SOURCE_REFRESH_REVIEWED_ON),
                        actionability = Actionability.IMMUTABLE,
                    )
                    any("schwarzman-degree-status") {
                        atomic(
                            id = "schwarzman-degree",
                            factKey = FactKey.DEGREE_COMPLETED,
                            operator = Operator.EQUALS,
                            expected = bool(true),
                            explanation = "A completed undergraduate degree meets the degree-presence check.",
                            sourceNote = checked(source, "Check Your Eligibility", SOURCE_REFRESH_REVIEWED_ON),
                            actionability = Actionability.TIME_DEPENDENT,
                            actionTemplateId = degreeAction.id,
                        )
                        manual(
                            id = "schwarzman-degree-before-start",
                            explanation = "Otherwise confirm all undergraduate degree requirements will be complete before 1 August 2028.",
                            sourceNote = checked(source, "Check Your Eligibility", SOURCE_REFRESH_REVIEWED_ON),
                        )
                    }
                    any("schwarzman-english-route") {
                        atomic(
                            id = "schwarzman-ielts-seven",
                            factKey = FactKey.IELTS_SCORE,
                            operator = Operator.GREATER_THAN_OR_EQUAL,
                            expected = number(7.0, "band"),
                            explanation = "IELTS 7.0 meets the published score for the IELTS route only.",
                            sourceNote = checked(source, "Language Skills", SOURCE_REFRESH_REVIEWED_ON),
                            actionability = Actionability.CONTROLLABLE,
                            actionTemplateId = ieltsAction.id,
                        )
                        manual(
                            id = "schwarzman-other-english-route",
                            explanation = "Otherwise verify an accepted TOEFL, Duolingo or Cambridge score, or a native/English-medium exemption.",
                            sourceNote = checked(source, "Language Skills", SOURCE_REFRESH_REVIEWED_ON),
                        )
                    }
                    manual(
                        id = "schwarzman-english-evidence",
                        explanation = "If using a test, confirm its report is valid; otherwise confirm exemption evidence.",
                        sourceNote = checked(source, "Language Skills", SOURCE_REFRESH_REVIEWED_ON),
                    )
                    manual(
                        id = "schwarzman-passport-route",
                        explanation = "Confirm the citizenship/passport application route and all programme-specific conditions.",
                        sourceNote = checked(source, "Personal Information and Next Selection Cycle", SOURCE_REFRESH_REVIEWED_ON),
                    )
                }
            }
            softCriteria.addAll(listOf("Leadership", "Intellectual ability", "Character and integrity"))
            readinessItems.addAll(
                listOf(
                    "U.S./Global applications are expected April–September 2027; confirm the exact closing date when published",
                    "IELTS 7.0 is one route; accepted tests and exemptions may also satisfy the language condition",
                ),
            )
            action(degreeAction)
            action(ieltsAction)
        }
    }

    private fun PackBuilder.eiffel() {
        val source = "https://www.southafrica.campusfrance.org/france-excellence-eiffel-scholarship"
        opportunity(
            id = "france-excellence-eiffel-2027",
            title = "France Excellence Eiffel Scholarship",
            provider = "French Ministry for Europe and Foreign Affairs · Campus France",
            category = OpportunityCategory.SCHOLARSHIP,
            cycle = "2027 campaign",
        ) {
            source(source, "2027 campaign timeline and how to apply", LocalDate.parse(EIFFEL_REVIEWED_ON))
            // The 6 January date is for institutions. Each student's university may close earlier.
            deadline { tba() }
            eligibility {
                manual(
                    id = "eiffel-official-check",
                    explanation = "Confirm foreign-national status, age band, eligible study field and nomination " +
                        "by a French institution under the 2027 regulations.",
                    sourceNote = checked(source, "2027 regulations and application process"),
                )
            }
            softCriteria.addAll(listOf("Academic excellence", "International profile"))
            readinessItems.addAll(
                listOf(
                    "Students cannot submit directly; a French higher-education institution must nominate",
                    "Ask the institution for its student deadline; institutions submit nominations to Campus France by 6 January 2027",
                ),
            )
        }
    }

    private fun PackBuilder.daadEpos() {
        val source =
            "https://www2.daad.de/deutschland/stipendium/datenbank/en/21148-scholarship-database.pdf?detail=50076777"
        val degreeAction = degreeAction("daad-epos", source)
        val experienceAction = experienceAction("daad-epos", 24.0, source)
        opportunity(
            id = "daad-epos-2027-2028",
            title = "DAAD EPOS",
            provider = "German Academic Exchange Service (DAAD)",
            category = OpportunityCategory.SCHOLARSHIP,
            cycle = "2027–2028 intake · course-specific",
        ) {
            source(source, "Requirements; EPOS course list", LocalDate.parse(SOURCE_REFRESH_REVIEWED_ON))
            deadline { tba() }
            eligibility {
                all("daad-root") {
                    atomic(
                        id = "daad-degree",
                        factKey = FactKey.DEGREE_COMPLETED,
                        operator = Operator.EQUALS,
                        expected = bool(true),
                        explanation = "Applicants need an appropriate first university degree.",
                        sourceNote = checked(source, "Requirements", SOURCE_REFRESH_REVIEWED_ON),
                        actionability = Actionability.TIME_DEPENDENT,
                        actionTemplateId = degreeAction.id,
                    )
                    atomic(
                        id = "daad-experience-duration",
                        factKey = FactKey.EXPERIENCE_MONTHS,
                        operator = Operator.GREATER_THAN_OR_EQUAL,
                        expected = number(24.0, "months"),
                        explanation = "The general EPOS criteria require at least two years of professional experience.",
                        sourceNote = checked(source, "Requirements", SOURCE_REFRESH_REVIEWED_ON),
                        actionability = Actionability.TIME_DEPENDENT,
                        actionTemplateId = experienceAction.id,
                    )
                    manual(
                        id = "daad-experience-relevance",
                        explanation = "Confirm that the experience is relevant, post-degree where required, and " +
                            "accepted by the selected EPOS course.",
                        sourceNote = checked(
                            source,
                            "Requirements and selected course page",
                            SOURCE_REFRESH_REVIEWED_ON,
                        ),
                    )
                    manual(
                        id = "daad-country-course",
                        explanation = "Confirm developing-country scope, course fit, language requirements and the course-specific deadline.",
                        sourceNote = checked(source, "EPOS country and course lists", SOURCE_REFRESH_REVIEWED_ON),
                    )
                }
            }
            softCriteria.addAll(listOf("Above-average academic performance", "Development motivation"))
            readinessItems.addAll(listOf("Apply to the selected university/course, not directly to DAAD unless instructed"))
            action(degreeAction)
            action(experienceAction)
        }
    }

    private fun PackBuilder.jjWbgsp() {
        val source = "https://www.worldbank.org/en/programs/scholarships/jj-wbgsp"
        val degreeAction = degreeAction("jj-wbgsp", source)
        val experienceAction = experienceAction("jj-wbgsp", 36.0, source)
        opportunity(
            id = "jj-wbgsp-2027-window-1",
            title = "Joint Japan/World Bank Graduate Scholarship",
            provider = "World Bank",
            category = OpportunityCategory.SCHOLARSHIP,
            cycle = "2027 application window 1",
        ) {
            source(source, "2027 Application Cycle; Who is Eligible to Apply?", LocalDate.parse(SOURCE_REFRESH_REVIEWED_ON))
            deadline { known("2027-02-26") }
            eligibility {
                all("jj-root") {
                    atomic(
                        id = "jj-degree",
                        factKey = FactKey.DEGREE_COMPLETED,
                        operator = Operator.EQUALS,
                        expected = bool(true),
                        explanation = "Applicants must hold a bachelor's degree or equivalent.",
                        sourceNote = checked(source, "Who is Eligible to Apply?", SOURCE_REFRESH_REVIEWED_ON),
                        actionability = Actionability.TIME_DEPENDENT,
                        actionTemplateId = degreeAction.id,
                    )
                    atomic(
                        id = "jj-experience-duration",
                        factKey = FactKey.EXPERIENCE_MONTHS,
                        operator = Operator.GREATER_THAN_OR_EQUAL,
                        expected = number(36.0, "months"),
                        explanation = "The broad criteria require at least three years of paid development-related employment.",
                        sourceNote = checked(source, "Who is Eligible to Apply?", SOURCE_REFRESH_REVIEWED_ON),
                        actionability = Actionability.TIME_DEPENDENT,
                        actionTemplateId = experienceAction.id,
                    )
                    manual(
                        id = "jj-degree-timing",
                        explanation = "Confirm the bachelor's degree was earned at least three years before the application deadline.",
                        sourceNote = checked(source, "Who is Eligible to Apply?", SOURCE_REFRESH_REVIEWED_ON),
                    )
                    manual(
                        id = "jj-experience-timing",
                        explanation = "Confirm the work is eligible, paid, development-related, post-degree and " +
                            "within the official six-year counting window.",
                        sourceNote = checked(
                            source,
                            "Who is Eligible to Apply? and application guidelines",
                            SOURCE_REFRESH_REVIEWED_ON,
                        ),
                    )
                    manual(
                        id = "jj-country-admission",
                        explanation = "Confirm nationality against the 2027 country list, no disqualifying dual citizenship, " +
                            "and unconditional admission to a participating programme. Viet Nam is absent from that list.",
                        sourceNote = checked(source, "2027 Application Cycle", SOURCE_REFRESH_REVIEWED_ON) +
                            " 2027 country list (https://www.worldbank.org/en/brief/2026/07/15/" +
                            "countries-eligible-for-jjwbgsp-scholarship), reviewed $SOURCE_REFRESH_REVIEWED_ON.",
                    )
                }
            }
            softCriteria.addAll(listOf("Commitment to supporting the applicant's country's development"))
            readinessItems.addAll(
                listOf(
                    "The university must shortlist the admitted candidate before the application link is issued",
                    "The 2027 eligible-country list does not include Viet Nam; check all citizenships before planning this path",
                ),
            )
            action(degreeAction)
            action(experienceAction)
        }
    }

    private fun PackBuilder.oecdInternship() {
        val source = "https://www.oecd.org/en/about/careers/internships.html"
        val enrollmentAction = enrollmentAction("oecd", source)
        val availabilityAction = availabilityAction(
            id = "oecd",
            programmeTitle = "OECD Internship Programme",
            targetRuleId = "oecd-availability",
            source = source,
        )
        opportunity(
            id = "oecd-internship-rolling",
            title = "OECD Internship Programme",
            provider = "Organisation for Economic Co-operation and Development",
            category = OpportunityCategory.INTERNSHIP,
            cycle = "Rolling applications",
        ) {
            source(source, "Who can apply?; Internship duration", LocalDate.parse(SOURCE_REFRESH_REVIEWED_ON))
            deadline { missing() }
            eligibility {
                all("oecd-root") {
                    atomic(
                        id = "oecd-enrolled",
                        factKey = FactKey.ENROLLED,
                        operator = Operator.EQUALS,
                        expected = bool(true),
                        explanation = "Applicants must remain enrolled in a full-time accredited degree for the internship.",
                        sourceNote = checked(source, "Who can apply?", SOURCE_REFRESH_REVIEWED_ON),
                        actionability = Actionability.TIME_DEPENDENT,
                        actionTemplateId = enrollmentAction.id,
                    )
                    atomic(
                        id = "oecd-availability",
                        factKey = FactKey.AVAILABLE_FULL_PERIOD,
                        operator = Operator.EQUALS,
                        expected = bool(true),
                        explanation = "Applicants must be available for at least one month.",
                        sourceNote = checked(
                            source,
                            "Who can apply? and Internship duration",
                            SOURCE_REFRESH_REVIEWED_ON,
                        ),
                        actionability = Actionability.CONTROLLABLE,
                        actionTemplateId = availabilityAction.id,
                    )
                    manual(
                        id = "oecd-field-language",
                        explanation = "Confirm a relevant study field and the official-language expectations for the intended team.",
                        sourceNote = checked(source, "Who can apply?", SOURCE_REFRESH_REVIEWED_ON),
                    )
                }
            }
            softCriteria.addAll(listOf("Quantitative and IT skills", "Drafting and communication skills"))
            readinessItems.addAll(listOf("State accurate availability; OECD recruits on a rolling basis"))
            action(enrollmentAction)
            action(availabilityAction.bind("oecd-internship-rolling", checkNotNull(eligibilityRule)))
        }
    }

    private fun PackBuilder.unicefInternship() {
        val source = "https://www.unicef.org/careers/internships"
        val enrollmentAction = enrollmentAction("unicef", source)
        opportunity(
            id = "unicef-internship-rolling",
            title = "UNICEF Internship Programme",
            provider = "UNICEF",
            category = OpportunityCategory.INTERNSHIP,
            cycle = "Vacancies throughout the year",
        ) {
            source(source, "Requirements; How to apply", LocalDate.parse(SOURCE_REFRESH_REVIEWED_ON))
            deadline { missing() }
            eligibility {
                all("unicef-root") {
                    atomic(
                        id = "unicef-age",
                        factKey = FactKey.AGE,
                        operator = Operator.GREATER_THAN_OR_EQUAL,
                        expected = number(18.0, "years"),
                        explanation = "Applicants must be at least 18 years old.",
                        sourceNote = checked(source, "Requirements", SOURCE_REFRESH_REVIEWED_ON),
                        actionability = Actionability.IMMUTABLE,
                    )
                    any("unicef-study-status") {
                        atomic(
                            id = "unicef-enrolled",
                            factKey = FactKey.ENROLLED,
                            operator = Operator.EQUALS,
                            expected = bool(true),
                            explanation = "Current undergraduate, graduate or PhD enrollment satisfies the study-status branch.",
                            sourceNote = checked(source, "Requirements", SOURCE_REFRESH_REVIEWED_ON),
                            actionability = Actionability.TIME_DEPENDENT,
                            actionTemplateId = enrollmentAction.id,
                        )
                        manual(
                            id = "unicef-recent-graduate",
                            explanation = "Otherwise confirm graduation within the past two years.",
                            sourceNote = checked(source, "Requirements", SOURCE_REFRESH_REVIEWED_ON),
                        )
                    }
                    manual(
                        id = "unicef-language-family",
                        explanation = "Confirm the vacancy language requirement and UNICEF family/reporting-line conflict rules.",
                        sourceNote = checked(source, "Requirements", SOURCE_REFRESH_REVIEWED_ON),
                    )
                }
            }
            softCriteria.addAll(listOf("Strong academic performance", "Motivation", "Relevant professional experience"))
            readinessItems.addAll(listOf("Review the requirements and deadline of the specific vacancy"))
            action(enrollmentAction)
        }
    }

    private fun PackBuilder.worldBankTreasury() {
        val source = "https://treasury.worldbank.org/en/about/unit/treasury/about/student-and-graduate-careers"
        val enrollmentAction = enrollmentAction("world-bank-treasury", source)
        val availabilityAction = availabilityAction(
            id = "world-bank-treasury",
            programmeTitle = "World Bank Treasury Summer Internship",
            targetRuleId = "world-bank-treasury-availability",
            source = source,
        )
        opportunity(
            id = "world-bank-treasury-summer-internship-2027",
            title = "World Bank Treasury Summer Internship",
            provider = "World Bank Treasury",
            category = OpportunityCategory.INTERNSHIP,
            cycle = "2027 summer internship",
        ) {
            source(source, "Internship Calendar; Eligibility", LocalDate.parse(REVIEWED_ON))
            deadline { tba() }
            eligibility {
                all("world-bank-treasury-root") {
                    atomic(
                        id = "world-bank-treasury-enrolled",
                        factKey = FactKey.ENROLLED,
                        operator = Operator.EQUALS,
                        expected = bool(true),
                        explanation = "Applicants must be enrolled in the required stage of a four-year-equivalent degree.",
                        sourceNote = checked(source, "Eligibility"),
                        actionability = Actionability.TIME_DEPENDENT,
                        actionTemplateId = enrollmentAction.id,
                    )
                    atomic(
                        id = "world-bank-treasury-availability",
                        factKey = FactKey.AVAILABLE_FULL_PERIOD,
                        operator = Operator.EQUALS,
                        expected = bool(true),
                        explanation = "Applicants must be available full-time for the entire internship.",
                        sourceNote = checked(source, "Eligibility and Internship Calendar"),
                        actionability = Actionability.CONTROLLABLE,
                        actionTemplateId = availabilityAction.id,
                    )
                    manual(
                        id = "world-bank-treasury-stage",
                        explanation = "Confirm the exact graduation window, good standing, relevant field and " +
                            "absence of concurrent work or internship.",
                        sourceNote = checked(source, "Eligibility"),
                    )
                }
            }
            softCriteria.addAll(listOf("Interest in finance, business or economics", "Finance and technology strengths"))
            readinessItems.addAll(listOf("Confirm the application close date on the live page"))
            action(enrollmentAction)
            action(availabilityAction.bind("world-bank-treasury-summer-internship-2027", checkNotNull(eligibilityRule)))
        }
    }

    private fun PackBuilder.esaInternship() {
        val source = "https://www.esa.int/About_Us/Careers_at_ESA/Student_Internships2"
        val enrollmentAction = enrollmentAction("esa", source)
        val availabilityAction = availabilityAction(
            id = "esa",
            programmeTitle = "ESA Student Internships",
            targetRuleId = "esa-availability",
            source = source,
        )
        opportunity(
            id = "esa-student-internships",
            title = "ESA Student Internships",
            provider = "European Space Agency",
            category = OpportunityCategory.INTERNSHIP,
            cycle = "Vacancy-specific next cycle",
        ) {
            source(source, "Who can apply?", LocalDate.parse(REVIEWED_ON))
            deadline { tba() }
            eligibility {
                all("esa-root") {
                    atomic(
                        id = "esa-enrolled",
                        factKey = FactKey.ENROLLED,
                        operator = Operator.EQUALS,
                        expected = bool(true),
                        explanation = "Applicants must retain university student status for the full internship.",
                        sourceNote = checked(source, "Who can apply?"),
                        actionability = Actionability.TIME_DEPENDENT,
                        actionTemplateId = enrollmentAction.id,
                    )
                    atomic(
                        id = "esa-availability",
                        factKey = FactKey.AVAILABLE_FULL_PERIOD,
                        operator = Operator.EQUALS,
                        expected = bool(true),
                        explanation = "The selected placement lasts between three and six months.",
                        sourceNote = checked(source, "Who can apply? and placement duration"),
                        actionability = Actionability.CONTROLLABLE,
                        actionTemplateId = availabilityAction.id,
                    )
                    manual(
                        id = "esa-citizenship-vacancy",
                        explanation = "Confirm eligible citizenship and every requirement in the selected ESA vacancy.",
                        sourceNote = checked(source, "Who can apply?"),
                    )
                }
            }
            softCriteria.addAll(listOf("Final or second-to-last year of a master's course is preferred"))
            readinessItems.addAll(listOf("Use the citizenship list and deadline in the selected vacancy"))
            action(enrollmentAction)
            action(availabilityAction.bind("esa-student-internships", checkNotNull(eligibilityRule)))
        }
    }

    private fun PackBuilder.cernOpenlab() {
        val source = "https://jobs.smartrecruiters.com/cern/744000159596524"
        val programmeSource = "https://openlab.web.cern.ch/cern-openlab-summer-student-programme/"
        val enrollmentAction = enrollmentAction("cern-openlab", source)
        val availabilityAction = availabilityAction(
            id = "cern-openlab",
            programmeTitle = "CERN openlab Summer Student Programme",
            targetRuleId = "cern-openlab-availability",
            source = source,
        )
        opportunity(
            id = "cern-openlab-summer-student-2027",
            title = "CERN openlab Summer Student Programme",
            provider = "CERN openlab",
            category = OpportunityCategory.ACADEMIC_PROGRAM,
            cycle = "Summer 2027",
        ) {
            source(source, "Qualifications; Additional Information", LocalDate.parse(CERN_REVIEWED_ON))
            deadline { known("2027-01-21") }
            eligibility {
                all("cern-openlab-root") {
                    atomic(
                        id = "cern-openlab-enrolled",
                        factKey = FactKey.ENROLLED,
                        operator = Operator.EQUALS,
                        expected = bool(true),
                        explanation = "The programme is for BSc or MSc students.",
                        sourceNote = checked(programmeSource, "Who can apply?"),
                        actionability = Actionability.TIME_DEPENDENT,
                        actionTemplateId = enrollmentAction.id,
                    )
                    atomic(
                        id = "cern-openlab-availability",
                        factKey = FactKey.AVAILABLE_FULL_PERIOD,
                        operator = Operator.EQUALS,
                        expected = bool(true),
                        explanation = "Participants must be available for the nine-week June–August programme.",
                        sourceNote = checked(programmeSource, "Who can apply? and programme duration"),
                        actionability = Actionability.CONTROLLABLE,
                        actionTemplateId = availabilityAction.id,
                    )
                    manual(
                        id = "cern-openlab-study-profile",
                        explanation = "Confirm six semesters of full-time study in an accepted field by summer 2027, " +
                            "continued enrollment, prior CERN participation limits and good English. " +
                            "CERN assesses the computing profile.",
                        sourceNote = checked(source, "Qualifications", CERN_REVIEWED_ON),
                    )
                }
            }
            softCriteria.addAll(listOf("Strong computing profile"))
            readinessItems.addAll(
                listOf(
                    "Prepare a CV, official transcript and academic reference letter",
                    "Apply by 21 January 2027, 23:59 Geneva time; verify the live listing before submitting",
                    "See the programme overview at $programmeSource",
                ),
            )
            action(enrollmentAction)
            action(availabilityAction.bind("cern-openlab-summer-student-2027", checkNotNull(eligibilityRule)))
        }
    }

    private fun PackBuilder.googleSummerOfCode() {
        val source = "https://developers.google.com/open-source/gsoc/faq"
        opportunity(
            id = "google-summer-of-code-next-cycle",
            title = "Google Summer of Code",
            provider = "Google Open Source Programs Office",
            category = OpportunityCategory.ACADEMIC_PROGRAM,
            cycle = "Next contributor cycle · timeline to be confirmed",
        ) {
            source(source, "Eligibility; GSoC Contributor Applicants", LocalDate.parse(REVIEWED_ON))
            deadline { tba() }
            eligibility {
                all("gsoc-root") {
                    atomic(
                        id = "gsoc-age",
                        factKey = FactKey.AGE,
                        operator = Operator.GREATER_THAN_OR_EQUAL,
                        expected = number(18.0, "years"),
                        explanation = "Contributors must be at least 18 at registration.",
                        sourceNote = checked(source, "Eligibility"),
                        actionability = Actionability.IMMUTABLE,
                    )
                    manual(
                        id = "gsoc-context",
                        explanation = "Confirm work eligibility, non-embargoed residence, beginner status, prior " +
                            "acceptance count and no prior mentor/admin conflict.",
                        sourceNote = checked(source, "Eligibility"),
                    )
                }
            }
            softCriteria.addAll(listOf("Early mentor interaction", "Detailed goals, methodology and timeline"))
            readinessItems.addAll(
                listOf(
                    "Choose a participating organization and follow its language and AI-use policies",
                    "Confirm the contributor application window on the current timeline",
                ),
            )
        }
    }

    private fun PackBuilder.swiftStudentChallenge() {
        val source = "https://developer.apple.com/swift-student-challenge/policy/"
        val enrollmentAction = enrollmentAction("swift-student-challenge", source)
        opportunity(
            id = "swift-student-challenge-2026",
            title = "Swift Student Challenge",
            provider = "Apple",
            category = OpportunityCategory.STUDENT_CONTEST,
            cycle = "2026 terms · archived",
        ) {
            source(source, "Deadlines; Application Eligibility Requirements; Submission Requirements", LocalDate.parse(REVIEWED_ON))
            deadline { known("2026-02-28") }
            eligibility {
                all("swift-challenge-root") {
                    any("swift-challenge-education-status") {
                        atomic(
                            id = "swift-challenge-enrolled",
                            factKey = FactKey.ENROLLED,
                            operator = Operator.EQUALS,
                            expected = bool(true),
                            explanation = "Current enrollment in an accepted education path satisfies one education-status branch.",
                            sourceNote = checked(source, "Application Eligibility Requirements"),
                            actionability = Actionability.TIME_DEPENDENT,
                            actionTemplateId = enrollmentAction.id,
                        )
                        manual(
                            id = "swift-challenge-other-education",
                            explanation = "Otherwise confirm one of Apple's recent-graduate, homeschool, academy or " +
                                "STEM-organization branches.",
                            sourceNote = checked(source, "Application Eligibility Requirements"),
                        )
                    }
                    manual(
                        id = "swift-challenge-age-context",
                        explanation = "Confirm the country-specific minimum age, developer registration, employment " +
                            "exclusions and award-history limits.",
                        sourceNote = checked(source, "Application Eligibility Requirements"),
                    )
                    manual(
                        id = "swift-challenge-artifact",
                        explanation = "Confirm the solo offline Swift Playground app and all current artifact and " +
                            "proof-of-enrollment requirements.",
                        sourceNote = checked(source, "Submission Requirements"),
                    )
                }
            }
            softCriteria.addAll(listOf("Technical accomplishment", "Creativity", "Written explanation"))
            readinessItems.addAll(listOf("This archived record must not be reused for a future challenge without new terms"))
            action(enrollmentAction)
            archived = true
        }
    }

    private fun degreeAction(id: String, source: String): ActionTemplate = action(
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

    private fun enrollmentAction(id: String, source: String): ActionTemplate = action(
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
        programmeTitle: String,
        targetRuleId: String,
        source: String,
    ): ScopedActionSpec = ScopedActionSpec(
        id = "$id-full-period-availability",
        title = "Confirm full-period availability for $programmeTitle",
        targetRuleId = targetRuleId,
        targetFactKey = FactKey.AVAILABLE_FULL_PERIOD,
        targetValue = bool(true),
        targetActionability = Actionability.CONTROLLABLE,
        minimumDurationDays = 14,
        effort = EffortLevel.LOW,
        costBand = CostBand.FREE,
        completionEvidenceTypes = setOf("calendar plan", "study or employer approval"),
        sourceNote = planningNote(source) +
            " The 14-day confirmation lead time is a Porta assumption, not the programme duration or a provider commitment.",
    )

    private fun experienceAction(
        id: String,
        months: Double,
        source: String,
    ): ActionTemplate = action(
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

    private fun ieltsAction(
        id: String,
        band: Double,
        source: String,
    ): ActionTemplate = action(
        id = "$id-ielts-${band.toInt()}-band",
        title = "Raise IELTS to ${band.toInt()}.0 band",
        targetFactKey = FactKey.IELTS_SCORE,
        targetValue = number(band, "band"),
        targetActionability = Actionability.CONTROLLABLE,
        minimumDurationDays = 45,
        effort = EffortLevel.MEDIUM,
        costBand = CostBand.LOW,
        evidence = setOf("official IELTS score report"),
        sourceNote = planningNote(source),
    )

    private fun action(
        id: String,
        title: String,
        targetFactKey: FactKey,
        targetValue: FactValue,
        targetActionability: Actionability,
        minimumDurationDays: Int,
        effort: EffortLevel,
        costBand: CostBand,
        evidence: Set<String>,
        sourceNote: String,
    ): ActionTemplate = ActionTemplate(
        id = id,
        title = title,
        targetFactKey = targetFactKey,
        targetValue = targetValue,
        targetActionability = targetActionability,
        minimumDurationDays = minimumDurationDays,
        effort = effort,
        costBand = costBand,
        completionEvidenceTypes = evidence,
        sourceNote = sourceNote,
    )

    private data class ScopedActionSpec(
        val id: String,
        val title: String,
        val targetRuleId: String,
        val targetFactKey: FactKey,
        val targetValue: FactValue,
        val targetActionability: Actionability,
        val minimumDurationDays: Int,
        val effort: EffortLevel,
        val costBand: CostBand,
        val completionEvidenceTypes: Set<String>,
        val sourceNote: String,
    ) {
        fun bind(opportunityId: String, eligibilityRule: RuleNode): ActionTemplate {
            val rule = checkNotNull(eligibilityRule.findAtomicRule(targetRuleId))
            return ActionTemplate(
                id = id,
                title = title,
                target = ActionTarget.OpportunityRuleTarget(
                    opportunityId = opportunityId,
                    ruleId = targetRuleId,
                    ruleFingerprint = rule.fingerprint(),
                    factKey = targetFactKey,
                    value = targetValue,
                ),
                targetActionability = targetActionability,
                minimumDurationDays = minimumDurationDays,
                effort = effort,
                costBand = costBand,
                completionEvidenceTypes = completionEvidenceTypes,
                sourceNote = sourceNote,
            )
        }
    }

    private fun bool(value: Boolean): FactValue = FactValue.BooleanValue(value)

    private fun date(value: String): FactValue = FactValue.DateValue(LocalDate.parse(value))

    private fun number(amount: Double, unit: String): FactValue = FactValue.NumberValue(amount, unit)

    private fun checked(
        source: String,
        section: String,
        reviewedOn: String = REVIEWED_ON,
    ): String = "$section on the official provider page ($source), reviewed $reviewedOn."

    private fun planningNote(
        source: String,
    ): String = "The official source establishes the criterion ($source). " +
        "Duration, effort and cost are transparent Porta planning estimates."
}
