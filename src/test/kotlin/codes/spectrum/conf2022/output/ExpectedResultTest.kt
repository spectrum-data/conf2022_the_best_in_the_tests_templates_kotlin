package codes.spectrum.conf2022.output

import codes.spectrum.conf2022.base.doc_type.DocType
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe

class ExpectedResultTest : FunSpec() {
    init {
        val correctInput = "1"
        val correctConstraints = "=="
        val correctDocType = DocType.UNDEFINED.toString()

        context("извлечения ограничения на вхождения и на порядок вхождения") {
            test("'==' - исключительно ожидаемый набор в указанном порядке") {
                ExpectedResult.parse("$correctInput==$correctDocType").also { result ->
                    result.isExactly.shouldBeTrue()
                    result.isOrderRequired.shouldBeTrue()
                }
            }

            test("'~=' - ожидаемый набор содержится в итоговой выборке в указанном порядке") {
                ExpectedResult.parse("$correctInput~=$correctDocType").also { result ->
                    result.isExactly.shouldBeFalse()
                    result.isOrderRequired.shouldBeTrue()
                }
            }

            test("'=?' - ожидаемый набор содержится в итоговой выборке в указанном порядке") {
                ExpectedResult.parse("$correctInput=?$correctDocType").also { result ->
                    result.isExactly.shouldBeTrue()
                    result.isOrderRequired.shouldBeFalse()
                }
            }

            test("'~?' - ожидаемый набор содержится в итоговой выборке в указанном порядке") {
                ExpectedResult.parse("$correctInput~?$correctDocType").also { result ->
                    result.isExactly.shouldBeFalse()
                    result.isOrderRequired.shouldBeFalse()
                }
            }
        }

        context("парсит все типы документов") {
            DocType.values().forEach { expectedDocType ->
                test("корректно парсит ${expectedDocType.name}") {
                    ExpectedResult.parse("$correctInput==${expectedDocType.name}").result.single().docType shouldBe expectedDocType
                }
            }
        }
    }
}
