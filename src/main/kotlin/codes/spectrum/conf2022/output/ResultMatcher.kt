package codes.spectrum.conf2022.output

import codes.spectrum.conf2022.base.TBITConfig

class ResultMatcher(val config: TBITConfig) {
    fun match(context: MatcherContext): TestMatch {
        return kotlin.runCatching {
            with(context) {
                when {
                    expectedResultDesc.isExactly() && expectedResultDesc.isOrder() -> matchExactlyOrdered()
                    expectedResultDesc.isExactly() && !expectedResultDesc.isOrder() -> matchExactlyNotOrdered()
                    expectedResultDesc.isContains() && expectedResultDesc.isOrder() -> matchContainsOrdered()
                    expectedResultDesc.isContains() && !expectedResultDesc.isOrder() -> matchContainsNotOrdered()

                    else -> error("Неопознанная комбинация описания теста: ${context.expectedResultDesc.entryConstraint} и ${context.expectedResultDesc.orderConditional}")
                }
            }
        }.getOrDefault(
            TestMatch(
                matchResult = MatchResult.ERROR
            )
        )
    }

    private fun MatcherContext.matchExactlyOrdered(): TestMatch {
        if (!isAnswersCountEqual()) {
            return TestMatch(
                matchResult = MatchResult.FAIL,
                message = INCORRECT_ANSWERS_COUNT_MESSAGE
            )
        }

        if (!isContainsInOrder()) {
            return TestMatch(
                matchResult = MatchResult.FAIL,
                message = INCORRECT_ANSWERS_COUNT_MESSAGE
            )
        }

        return TestMatch(matchResult = MatchResult.SUCCESS)
    }

    private fun MatcherContext.matchExactlyNotOrdered(): TestMatch {
        if (!isAnswersCountEqual()) {
            return TestMatch(
                matchResult = MatchResult.FAIL,
                message = INCORRECT_ANSWERS_COUNT_MESSAGE
            )
        }

        if (!isContainsDespiteOrder()) {
            return TestMatch(
                matchResult = MatchResult.FAIL,
                message = DOES_NOT_CONTAINS_CORRECT_ANSWER_MESSAGE
            )
        }

        return TestMatch(matchResult = MatchResult.SUCCESS)
    }

    private fun MatcherContext.matchContainsOrdered(): TestMatch {
        if (!isContainsInOrder()) {
            return TestMatch(
                matchResult = MatchResult.FAIL,
                message = INCORRECT_ANSWERS_COUNT_MESSAGE
            )
        }

        return TestMatch(matchResult = MatchResult.SUCCESS)
    }

    private fun MatcherContext.matchContainsNotOrdered(): TestMatch {
        if (!isContainsDespiteOrder()) {
            return TestMatch(
                matchResult = MatchResult.FAIL,
                message = DOES_NOT_CONTAINS_CORRECT_ANSWER_MESSAGE
            )
        }

        return TestMatch(matchResult = MatchResult.SUCCESS)
    }

    private fun MatcherContext.isAnswersCountEqual(): Boolean {
        return actualExtractedDocs.count() == expectedResultDesc.result.count()
    }

    private fun MatcherContext.isContainsInOrder(): Boolean {
        expectedResultDesc.result.forEachIndexed { i, expectedAnswer ->
            if (actualExtractedDocs[i] != expectedAnswer) return false
        }

        return true
    }

    private fun MatcherContext.isContainsDespiteOrder(): Boolean {
        expectedResultDesc.result.forEach { expectedAnswer ->
            if (!actualExtractedDocs.contains(expectedAnswer)) return false
        }

        return true
    }

    companion object {
        const val INCORRECT_ANSWERS_COUNT_MESSAGE = "Некорректное количество записей в ответе"
        const val DOES_NOT_CONTAINS_CORRECT_ANSWER_MESSAGE = "Не содержит один из ожидаемых ответов"
    }
}