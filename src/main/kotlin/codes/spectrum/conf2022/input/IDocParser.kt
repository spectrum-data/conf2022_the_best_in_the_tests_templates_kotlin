package codes.spectrum.conf2022.input

import codes.spectrum.conf2022.doc_type.DocType
import codes.spectrum.conf2022.output.ExpectedResult
import codes.spectrum.conf2022.output.ExtractedDocument
import kotlin.random.Random

interface IDocParser {
    fun parse(input: String): List<ExtractedDocument>
}

class RandomSuccessfulParser : IDocParser {
    override fun parse(input: String): List<ExtractedDocument> {
        return if (Random.nextBoolean()) {
            parseByExpectedResult(input)
        } else {
            emptyList()
        }
    }

    private fun parseByExpectedResult(input: String): List<ExtractedDocument> {
        val expectedResult = ExpectedResult.parse(input)

        return expectedResult.expected
    }
}