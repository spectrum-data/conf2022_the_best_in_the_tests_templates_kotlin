package codes.spectrum.conf2022.input

import codes.spectrum.conf2022.doc_type.DocType
import codes.spectrum.conf2022.output.ExtractedDocument

interface IDocParser {
    fun parse(input: String): List<ExtractedDocument>
}
