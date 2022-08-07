package codes.spectrum.conf2022.output

data class MatcherContext(
    val expectedResultDesc: ExpectedResult,
    val actualExtractedDocs: List<ExtractedDocument>
) {

}