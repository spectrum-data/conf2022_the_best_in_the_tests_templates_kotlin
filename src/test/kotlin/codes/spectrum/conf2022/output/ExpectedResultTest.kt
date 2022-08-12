package codes.spectrum.conf2022.output

import codes.spectrum.conf2022.base.doc_type.DocType
import io.kotest.core.spec.style.FunSpec
import io.kotest.inspectors.forAll
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldBeEmpty
import kotlinx.coroutines.launch

class ExpectedResultTest : FunSpec() {
    data class TestCase(
        val input: String,
        val expectedParsedDocs: List<ExtractedDocument>,
        val testName: String
    ) {
    }

    init {
        val correctInput = "1"
        val correctConstraints = "=="
        val correctDocType = DocType.UNDEFINED.toString()

        context("parse") {

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

            context("парсинг ожидаемых документов") {
                context("парсит все типы документов") {
                    DocType.values().forEach { expectedDocType ->
                        test("корректно парсит ${expectedDocType.name}") {
                            ExpectedResult.parse("$correctInput==${expectedDocType.name}").result.single().docType shouldBe expectedDocType
                        }
                    }
                }

                test("проверка сохранения очередности") {
                    val shuffledDocTypes = DocType.values().apply { shuffle() }

                    val inputString = "$correctInput$correctConstraints" +
                            shuffledDocTypes.map { it.name }.joinToString(ExpectedResult.EXPECTED_DOCUMENTS_SEPARATOR)

                    ExpectedResult.parse(inputString)
                        .result.forEachIndexed { index, extractedDocument ->
                            extractedDocument.value.shouldBeEmpty()
                            extractedDocument.docType shouldBe shuffledDocTypes[index]
                        }
                }

                listOf(
                    TestCase(
                        input = "$correctInput${correctConstraints}" + "INN_UL, PASSPORT_RF",
                        expectedParsedDocs = listOf(
                            ExtractedDocument(docType = DocType.INN_UL, value = ""),
                            ExtractedDocument(docType = DocType.PASSPORT_RF, value = ""),
                        ),
                        testName = "Документы без значений"
                    ),

                    TestCase(
                        input = "$correctInput${correctConstraints}" + "INN_UL:0123456789, PASSPORT_RF",
                        expectedParsedDocs = listOf(
                            ExtractedDocument(docType = DocType.INN_UL, value = "0123456789"),
                            ExtractedDocument(docType = DocType.PASSPORT_RF, value = ""),
                        ),
                        testName = "Некоторые документ со значениями, некоторые без"
                    ),

                    TestCase(
                        input = "$correctInput${correctConstraints}" + "INN_UL:0123456789, PASSPORT_RF:9876543210",
                        expectedParsedDocs = listOf(
                            ExtractedDocument(docType = DocType.INN_UL, value = "0123456789"),
                            ExtractedDocument(docType = DocType.PASSPORT_RF, value = "9876543210"),
                        ),
                        testName = "Все документы со значениями"
                    ),

                    TestCase(
                        input = "$correctInput${correctConstraints}" + "PASSPORT_RF: 0123456789 ",
                        expectedParsedDocs = listOf(
                            ExtractedDocument(docType = DocType.PASSPORT_RF, value = "0123456789"),
                        ),
                        testName = "проверка на трим значения"
                    )
                ).forAll { testCase ->
                    launch {
                        test(name = testCase.testName) {
                            ExpectedResult.parse(testCase.input).result shouldContainExactly testCase.expectedParsedDocs
                        }
                    }
                }
            }
        }

        context("Проверка функции соответствия списка документов ожидаемому результату") {
            context("isExactly = true, isOrderRequired = true") {
                val exactlyInOrder = ExpectedResult(isExactly = true, isOrderRequired = true)

                test("Содержит именно тот набор в том же порядке - соответствует результату") {
                    val expectedResult = exactlyInOrder.copy(
                        result = listOf(
                            ExtractedDocument(docType = DocType.SNILS, value = "012345555"),
                            ExtractedDocument(docType = DocType.DRIVER_LICENSE, value = "14587999922")
                        )
                    )

                    val actualExtractedDocuments = listOf(
                        ExtractedDocument(docType = DocType.SNILS, value = "012345555"),
                        ExtractedDocument(docType = DocType.DRIVER_LICENSE, value = "14587999922")
                    )

                    expectedResult.match(actualExtractedDocuments).shouldBeTrue()
                }

                test("Содержит именно тот набор в другом порядке - не соответствует результату") {
                    val expectedResult = exactlyInOrder.copy(
                        result = listOf(
                            ExtractedDocument(docType = DocType.SNILS, value = "012345555"),
                            ExtractedDocument(docType = DocType.DRIVER_LICENSE, value = "14587999922")
                        )
                    )

                    val actualExtractedDocuments = listOf(
                        ExtractedDocument(docType = DocType.DRIVER_LICENSE, value = "14587999922"),
                        ExtractedDocument(docType = DocType.SNILS, value = "012345555"),
                    )

                    expectedResult.match(actualExtractedDocuments).shouldBeFalse()
                }
            }
        }
    }
}
