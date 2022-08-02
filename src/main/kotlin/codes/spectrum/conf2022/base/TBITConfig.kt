package codes.spectrum.conf2022.base

data class TBITConfig(
    val isTestMod: Boolean = false,
    val testInputFilePath: String,
    val testOutputFilePath: String,
    val withCsvHeader: Boolean,
    val separator: String
) {

}
