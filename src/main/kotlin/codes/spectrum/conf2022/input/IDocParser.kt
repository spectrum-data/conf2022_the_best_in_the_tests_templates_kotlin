package codes.spectrum.conf2022.input

import codes.spectrum.conf2022.base.doc_type.DocType
import codes.spectrum.conf2022.output.ExtractedDocument

interface IDocParser {
    fun parse(input: String): List<ExtractedDocument>
}

class MyDocParser : IDocParser {
    override fun parse(input: String): List<ExtractedDocument> {
        return listOf(parsePassport(input))
    }

    private fun parsePassport(input: String): ExtractedDocument {
        return ExtractedDocument(
            docType = DocType.PASSPORT,
            value = input.replace("\\D".toRegex(), "")
        )
    }
}