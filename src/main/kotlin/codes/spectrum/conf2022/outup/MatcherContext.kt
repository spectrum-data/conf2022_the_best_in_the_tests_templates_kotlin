package codes.spectrum.conf2022.outup

import codes.spectrum.conf2022.input.TestDesc

data class MatcherContext(
    val testDesc: TestDesc,
    val expectedResultDesc: TestResultDesc,
    val actualResultDesc: TestResultDesc
) {

}