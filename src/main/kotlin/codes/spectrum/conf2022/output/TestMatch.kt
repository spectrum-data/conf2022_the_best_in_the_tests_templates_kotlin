package codes.spectrum.conf2022.output

enum class MatchResult{
    SUCCESS,
    FAIL,
    ERROR,
    UNDEFINED
}

data class TestMatch(val matchResult : MatchResult = MatchResult.UNDEFINED, val message: String = "") {

}
