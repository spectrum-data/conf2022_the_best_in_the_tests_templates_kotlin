package codes.spectrum.conf2022.output

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe

class ExpectedResultParserTest : FunSpec() {
    val parser = ExpectedResultParser()

    init {
        test("строка пустая - выбрасывает исключение") {
            shouldThrow<IllegalStateException> {
                parser.parse("")
            }
        }

        test("строка содержит только пробельные символы - выбрасывает исключение") {
            val onlyWhiteSpacesString = "         \t"

            shouldThrow<IllegalStateException> {
                parser.parse(onlyWhiteSpacesString)
            }
        }

        context("Пустой список ожидаемых результатов - "){
            test("Строка состоит только из [] - содержит исключительно ожидаемый набор в любом порядке") {
                val squareBrackets = "[]"

                val parsedResultDesc = parser.parse(squareBrackets)

                parsedResultDesc.result.shouldBeEmpty()

                parsedResultDesc.entryConstraint shouldBe EntryConstraint.EXACTLY
                parsedResultDesc.orderConditional shouldBe OrderConditional.DOES_NOT_MATTER
            }

            test("Строка состоит только из () - ожидаемый набор содержится в итоговый выборке в любом порядке") {
                val roundBrackets = "()"

                val parsedResultDesc = parser.parse(roundBrackets)

                parsedResultDesc.result.shouldBeEmpty()

                parsedResultDesc.entryConstraint shouldBe EntryConstraint.CONTAINS
                parsedResultDesc.orderConditional shouldBe OrderConditional.DOES_NOT_MATTER
            }
        }
    }
}
