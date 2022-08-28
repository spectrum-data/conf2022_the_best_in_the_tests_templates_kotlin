package codes.spectrum.conf2022.output

import codes.spectrum.conf2022.doc_type.DocType
import io.kotest.core.spec.style.FunSpec
import io.kotest.core.spec.style.scopes.FunSpecContainerContext
import io.kotest.inspectors.forAll
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldBeEmpty
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class ExpectedResultTest : FunSpec() {
    init {
        val correctInput = "1"
        val correctConstraints = "=="
        val correctDocType = DocType.UNDEFINED.toString()

        context("parse") {
            fun FunSpecContainerContext.parsingTest(
                input: String,
                expectedParsedDocs: List<ExtractedDocument>,
                testName: String
            ): Unit {
                launch {
                    test(name = testName) {
                        ExpectedResult.parse(input).expected shouldContainExactly expectedParsedDocs
                    }
                }
            }


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
                            ExpectedResult.parse("$correctInput==${expectedDocType.name}").expected.single().docType shouldBe expectedDocType
                        }
                    }
                }

                test("проверка сохранения очередности") {
                    val shuffledDocTypes = DocType.values().apply { shuffle() }

                    val inputString = "$correctInput$correctConstraints" +
                            shuffledDocTypes.map { it.name }.joinToString(ExpectedResult.EXPECTED_DOCUMENTS_SEPARATOR)

                    ExpectedResult.parse(inputString)
                        .expected.forEachIndexed { index, extractedDocument ->
                            extractedDocument.value.shouldBeEmpty()
                            extractedDocument.docType shouldBe shuffledDocTypes[index]
                        }
                }

                parsingTest(
                    input = "$correctInput${correctConstraints}" + "INN_UL, PASSPORT_RF",
                    expectedParsedDocs = listOf(
                        ExtractedDocument(docType = DocType.INN_UL, value = ""),
                        ExtractedDocument(docType = DocType.PASSPORT_RF, value = ""),
                    ),
                    testName = "Документы без значений"
                )

                parsingTest(
                    input = "$correctInput${correctConstraints}" + "INN_UL:0123456789, PASSPORT_RF",
                    expectedParsedDocs = listOf(
                        ExtractedDocument(docType = DocType.INN_UL, value = "0123456789"),
                        ExtractedDocument(docType = DocType.PASSPORT_RF, value = ""),
                    ),
                    testName = "Некоторые документ со значениями, некоторые без"
                )

                parsingTest(
                    input = "$correctInput${correctConstraints}" + "INN_UL:0123456789, PASSPORT_RF:9876543210",
                    expectedParsedDocs = listOf(
                        ExtractedDocument(docType = DocType.INN_UL, value = "0123456789"),
                        ExtractedDocument(docType = DocType.PASSPORT_RF, value = "9876543210"),
                    ),
                    testName = "Все документы со значениями"
                )

                parsingTest(
                    input = "$correctInput${correctConstraints}" + "PASSPORT_RF: 0123456789 ",
                    expectedParsedDocs = listOf(
                        ExtractedDocument(docType = DocType.PASSPORT_RF, value = "0123456789"),
                    ),
                    testName = "проверка на трим значения"
                )
            }
        }

        context("match - Проверка функции соответствия списка документов ожидаемому результату") {
            fun FunSpecContainerContext.matchTest(
                testName: String,
                expectedResult: ExpectedResult,
                actualExtractedDocs: List<ExtractedDocument>,
                expectedMatchResult: Boolean
            ): Unit {
                launch {
                    test(testName) {
                        expectedResult.match(actualExtractedDocs).shouldBe(expectedMatchResult)
                    }
                }
            }

            val someDoc = ExtractedDocument(docType = DocType.SNILS, value = "012345555")
            val anotherSomeDoc = ExtractedDocument(docType = DocType.DRIVER_LICENSE, value = "14587999922")

            listOf(
                ExpectedResult(isExactly = true, isOrderRequired = true),
                ExpectedResult(isExactly = true, isOrderRequired = false),
                ExpectedResult(isExactly = false, isOrderRequired = true),
                ExpectedResult(isExactly = false, isOrderRequired = false),
            ).forAll { baseExpectedResult ->
                runBlocking {
                    context("isExactly = ${baseExpectedResult.isExactly}, isOrderRequired = ${baseExpectedResult.isOrderRequired}") {
                        runBlocking {
                            matchTest(
                                testName = "Содержит именно тот набор в том же порядке - соответствует результату",
                                expectedResult = baseExpectedResult.copy(expected = listOf(someDoc, anotherSomeDoc)),
                                actualExtractedDocs = listOf(someDoc, anotherSomeDoc),
                                expectedMatchResult = true,
                            )

                            matchTest(
                                testName = "Не содержит ожидаемый набор - не соответствует результату",
                                expectedResult = baseExpectedResult.copy(expected = listOf(someDoc, anotherSomeDoc)),
                                actualExtractedDocs = listOf(someDoc),
                                expectedMatchResult = false,
                            )

                            matchTest(
                                testName = "Содержит именно тот набор в другом порядке - ${if (baseExpectedResult.isOrderRequired) "не" else ""} соответствует результату",
                                expectedResult = baseExpectedResult.copy(expected = listOf(someDoc, anotherSomeDoc)),
                                actualExtractedDocs = listOf(anotherSomeDoc, someDoc),
                                expectedMatchResult = !baseExpectedResult.isOrderRequired,
                            )

                            matchTest(
                                testName = "Содержит ожидаемый набор в ожидаемом порядке и ДОП. элемент - ${if (baseExpectedResult.isExactly) "не" else ""} соответствует результату",
                                expectedResult = baseExpectedResult.copy(expected = listOf(someDoc, anotherSomeDoc)),
                                actualExtractedDocs = listOf(someDoc, ExtractedDocument(), anotherSomeDoc,),
                                expectedMatchResult = !baseExpectedResult.isExactly,
                            )

                            matchTest(
                                testName = "Содержит ожидаемый набор в другом порядке и ДОП. элемент - ${if (baseExpectedResult.isExactly || baseExpectedResult.isOrderRequired) "не" else ""} соответствует результату",
                                expectedResult = baseExpectedResult.copy(expected = listOf(someDoc, anotherSomeDoc)),
                                actualExtractedDocs = listOf(anotherSomeDoc, ExtractedDocument(), someDoc),
                                expectedMatchResult = !(baseExpectedResult.isExactly || baseExpectedResult.isOrderRequired),
                            )

                            matchTest(
                                testName = "Ожидается пустой набор - проверяется пустой набор - соответствует результату",
                                expectedResult = baseExpectedResult.copy(expected = emptyList()),
                                actualExtractedDocs = emptyList(),
                                expectedMatchResult = true,
                            )

                            matchTest(
                                testName = "Ожидается пустой набор - проверяется не пустой набор - ${if (baseExpectedResult.isExactly) "не" else ""} соответствует результату",
                                expectedResult = baseExpectedResult.copy(expected = emptyList()),
                                actualExtractedDocs = listOf(someDoc),
                                expectedMatchResult = !baseExpectedResult.isExactly,
                            )
                        }
                    }
                }
            }
        }
    }
}
