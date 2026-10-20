# Official Kotlin conventions — audit reference

> Source: [Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html)
> Page date: 12 August 2026
> Verified: 20 October 2026

This is a review map, not a replacement for the official guide. Reopen the source for final contest auditing.

## What automation covers

`./gradlew spotlessCheck` applies the repository's configured ktlint rules to Kotlin and Gradle Kotlin files. It is
strong evidence for indentation, whitespace, braces, wrapping covered by enabled rules, and basic formatting.
It cannot establish semantic naming quality, sensible organization, immutability, appropriate language idioms,
or readable architecture.

The root `build.gradle.kts` intentionally disables these ktlint rules:

| Disabled rule | Audit interpretation |
|---|---|
| `function-naming` | Permit PascalCase Compose and backtick tests; manually reject unjustified non-Composable names. |
| `value-parameter-comment` | Do not add noisy parameter comments; judge clarity through names and focused KDoc. |
| `max-line-length` | The editor target is 140, but the rule is disabled; manually flag lines that harm comprehension. |
| `no-wildcard-imports` | Do not treat a wildcard as an automatic failure; flag ambiguity or inconsistent import style. |
| `property-naming` | Allow framework/generated edge cases; manually enforce conventional names in handwritten code. |

## Manual audit map

### Source organization

- Package paths correspond to source directories and packages remain lowercase.
- A single main class/interface normally matches its UpperCamelCase filename.
- Multi-declaration files use a descriptive name, not vague names such as `Util`.
- Closely related declarations may share a reasonably sized file; unrelated extension dumping grounds do not.
- Platform files with top-level declarations carry their source-set suffix, such as `Platform.jvm.kt` or
  `Platform.android.kt`; common files do not.

### Naming

- Classes/objects and Unit-returning Composables use UpperCamelCase.
- Functions, ordinary properties, and local variables use lower camel case.
- True constants use SCREAMING_SNAKE_CASE; mutable or behavioral object properties do not impersonate constants.
- Names express role and behavior; avoid unexplained `Manager`, `Wrapper`, `Helper`, and `Util` labels.
- Mutating versus copy-returning operations are distinguishable through names.

### File and declaration layout

- Class order is properties/initializers, secondary constructors, methods, then companion object.
- Related methods remain together; overloads remain adjacent; interface implementations follow interface order
  when practical.
- Long constructors/functions use four-space continuation indentation and readable line breaks.
- Annotations precede modifiers; redundant `public`, `Unit`, semicolons, and syntax are omitted for app code.
- Declaration-site trailing commas are encouraged but optional.

### Idiomatic Kotlin

- Prefer `val` and read-only collection interfaces unless mutation is required.
- Prefer default arguments over trivial overloads.
- Use expression bodies for genuinely simple single-expression functions.
- Name Boolean and same-primitive arguments at call sites when their meaning is not obvious.
- Prefer `if` for binary alternatives and `when` for three or more meaningful branches.
- Use higher-order collection operations when they improve clarity, but retain loops when clearer or cheaper;
  avoid `forEach` as a reflex.
- Use `..<` for open-ended ranges, string templates instead of concatenation, and multiline strings when suitable.
- Use explicit parameters in nested lambdas and avoid multiple labeled returns.
- Prefer a property over a no-argument function only when the operation is cheap, stable, and non-throwing.
- Restrict extension visibility to avoid API pollution and use scope functions for a clear purpose.

### Documentation

- KDoc explains non-obvious contracts, public domain algorithms, rule semantics, and surprising constraints; it
  does not narrate obvious code.
- Prefer prose with links to parameters over repetitive `@param`/`@return` tags.
- Comments explain why, sources, or invariants rather than translating the next line into English.

## Contest evidence

For the 20% criterion, preserve evidence beyond a green task:

- `spotlessCheck` output;
- convention-review findings resolved or explicitly justified;
- representative shared-domain and Compose files with clear names and structure;
- tests whose names describe behavior;
- a short README section describing automated formatting and validation commands.
