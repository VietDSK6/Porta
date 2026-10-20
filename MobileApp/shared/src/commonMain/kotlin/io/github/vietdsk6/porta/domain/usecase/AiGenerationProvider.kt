package io.github.vietdsk6.porta.domain.usecase

import io.github.vietdsk6.porta.domain.model.generation.GenerationInput
import io.github.vietdsk6.porta.domain.model.generation.GenerationInputBuilder
import io.github.vietdsk6.porta.domain.model.generation.GenerationOutput
import io.github.vietdsk6.porta.domain.model.generation.generationInput

/**
 * Abstraction over the AI backend that turns a [GenerationInput] into a [GenerationOutput].
 * Implemented per provider in `data/source/ai/` (e.g. Replicate, OpenAI) and bound in DI.
 */
interface AiGenerationProvider {
    suspend fun generate(input: GenerationInput): Result<GenerationOutput>
}

/** Convenience overload that builds the input inline with the [generationInput] DSL. */
suspend fun AiGenerationProvider.generate(block: GenerationInputBuilder.() -> Unit): Result<GenerationOutput> {
    val input = generationInput(block)
    return generate(input)
}
