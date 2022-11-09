package codes.spectrum.conf2022.output

import codes.spectrum.conf2022.InternalTest
import codes.spectrum.conf2022.doc_type.DocType
import codes.spectrum.conf2022.output.ExpectedResult.Companion.INPUT_STRUCTURE_REGEX
import io.kotest.assertions.throwables.shouldThrowMessage
import io.kotest.core.spec.style.scopes.FunSpecContainerContext
import io.kotest.inspectors.forAll
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldBeEmpty
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

internal class ExpectedResultTest : InternalTest() {
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
                    input = "иннЮЛ, паспорт России" + correctConstraints + "INN_UL, PASSPORT_RF",
                    expectedParsedDocs = listOf(
                        ExtractedDocument(docType = DocType.INN_UL, value = "", isValidSetup = false),
                        ExtractedDocument(docType = DocType.PASSPORT_RF, value = "", isValidSetup = false),
                    ),
                    testName = "Документы без значений"
                )

                parsingTest(
                    input = "инн ЮЛ 0123456789, паспортРФ" + correctConstraints + "INN_Ul+:0123456789, PASSPORT_RF",
                    expectedParsedDocs = listOf(
                        ExtractedDocument(
                            docType = DocType.INN_UL,
                            value = "0123456789",
                            isValidSetup = true,
                            isValid = true
                        ),
                        ExtractedDocument(docType = DocType.PASSPORT_RF, value = "", isValidSetup = false),
                    ),
                    testName = "Некоторые документ со значениями, некоторые без"
                )

                parsingTest(
                    input = "ООО Рога и Копыта - 0123456789, Иванов И.И. 9876543210" + correctConstraints + "INN_UL+:9876543210, PASSPORT_RF-:0123456789",
                    expectedParsedDocs = listOf(
                        ExtractedDocument(
                            docType = DocType.INN_UL,
                            value = "9876543210",
                            isValidSetup = true,
                            isValid = true
                        ),
                        ExtractedDocument(
                            docType = DocType.PASSPORT_RF,
                            value = "0123456789",
                            isValidSetup = true,
                            isValid = false
                        ),
                    ),
                    testName = "Все документы со значениями"
                )

                parsingTest(
                    input = "паспортРФ-0123456789" + correctConstraints + "PASSPORT_RF",
                    expectedParsedDocs = listOf(
                        ExtractedDocument(docType = DocType.PASSPORT_RF, isValidSetup = false),
                    ),
                    testName = "не указана валидация - заполняется флаг, что валидность не установлена"
                )

                parsingTest(
                    input = "паспортРФ-0123456789" + correctConstraints + "PASSPORT_RF+:0123456789",
                    expectedParsedDocs = listOf(
                        ExtractedDocument(
                            docType = DocType.PASSPORT_RF,
                            value = "0123456789",
                            isValidSetup = true,
                            isValid = true,
                        ),
                    ),
                    testName = "указана валидность документа - заполняется флаг, что валидность установлена и что документ валиден"
                )

                parsingTest(
                    input = "паспортРФ-0123456789" + correctConstraints + "PASSPORT_RF-:0123456789",
                    expectedParsedDocs = listOf(
                        ExtractedDocument(
                            docType = DocType.PASSPORT_RF,
                            value = "0123456789",
                            isValidSetup = true,
                            isValid = false,
                        ),
                    ),
                    testName = "указана не валидность документа - заполняется флаг, что валидность установлена и что документ не валиден"
                )

                parsingTest(
                    input = "паспорт рф01234567890==PASSPORT_RF:0123456789",
                    expectedParsedDocs = listOf(
                        ExtractedDocument(
                            docType = DocType.PASSPORT_RF,
                            value = "0123456789",
                            isValidSetup = false,
                        ),
                    ),
                    testName = "Отсутсвует символ валидации и указан номер после двоеточия - всё ок"
                )

                test("номер документа указан не в нормализованном формате - ошибка") {
                    val nonNormalisePassportRf = "0123 456789"

                    DocType.PASSPORT_RF.normaliseRegex.matches(nonNormalisePassportRf).shouldBeFalse()

                    val input = "паспорт рф01234567890==PASSPORT_RF:${nonNormalisePassportRf}"

                    shouldThrowMessage("Указанный номер - '0123 456789' - не соответствует нормализованному формату ${DocType.PASSPORT_RF.normaliseRegex} для ${DocType.PASSPORT_RF}") {
                        ExpectedResult.parse(input)
                    }
                }
            }

            context("Тесты на регулярку структуры описания теста") {
                val inputRegex = INPUT_STRUCTURE_REGEX.toRegex()

                test("Корректные ограничения на вхождение - проходит регулярку") {
                    inputRegex.matches("паспорт рф==PASSPORT_RF").shouldBeTrue()
                    inputRegex.matches("паспорт рф~=PASSPORT_RF").shouldBeTrue()
                    inputRegex.matches("паспорт рф=?PASSPORT_RF").shouldBeTrue()
                    inputRegex.matches("паспорт рф~?PASSPORT_RF").shouldBeTrue()
                }

                test("Затроенные символы ограничения на вхождение - непроходит регулярку") {
                    inputRegex.matches("паспорт рф===PASSPORT_RF").shouldBeFalse()
                    inputRegex.matches("паспорт рф~==PASSPORT_RF").shouldBeFalse()
                    inputRegex.matches("паспорт рф=??PASSPORT_RF").shouldBeFalse()
                    inputRegex.matches("паспорт рф~??PASSPORT_RF").shouldBeFalse()
                }
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
                                actualExtractedDocs = listOf(someDoc, ExtractedDocument(), anotherSomeDoc),
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

                            matchTest(
                                testName = "Ожидается только док тайп - результат: доктайп + номер - соответствует результату",
                                expectedResult = baseExpectedResult.copy(
                                    expected = listOf(
                                        someDoc.copy(
                                            value = "",
                                            isValidSetup = false,
                                            isValid = false,
                                        )
                                    )
                                ),
                                actualExtractedDocs = listOf(
                                    someDoc.copy(
                                        value = "0123456789",
                                        isValid = true,
                                        isValidSetup = true
                                    )
                                ),
                                expectedMatchResult = true,
                            )

                            matchTest(
                                testName = "Ожидается только док тайп - результат: доктайп не совпадает - не соответствует результату",
                                expectedResult = baseExpectedResult.copy(
                                    expected = listOf(
                                        someDoc
                                    )
                                ),
                                actualExtractedDocs = listOf(
                                    someDoc.copy(
                                        docType = DocType.INN_UL
                                    )
                                ),
                                expectedMatchResult = false,
                            )

                            matchTest(
                                testName = "Ожидается док тайп и номер - результат: совпадает доктайп, номер + валидация - соответствует результату",
                                expectedResult = baseExpectedResult.copy(
                                    expected = listOf(
                                        someDoc
                                    )
                                ),
                                actualExtractedDocs = listOf(
                                    someDoc.copy(
                                        isValid = !someDoc.isValid
                                    )
                                ),
                                expectedMatchResult = true,
                            )

                            matchTest(
                                testName = "Ожидается док тайп и номер - результат: номер не совпадает - не соответствует результату",
                                expectedResult = baseExpectedResult.copy(
                                    expected = listOf(
                                        someDoc.copy(isValidSetup = false, isValid = false)
                                    )
                                ),
                                actualExtractedDocs = listOf(
                                    someDoc.copy(
                                        value = someDoc.value.dropLast(1)
                                    )
                                ),
                                expectedMatchResult = false,
                            )

                            matchTest(
                                testName = "Ожидается док тайп и валидация - результат: номер не совпадает - соответствует результату",
                                expectedResult = baseExpectedResult.copy(
                                    expected = listOf(
                                        someDoc.copy(value = "", isValidSetup = true, isValid = true)
                                    )
                                ),
                                actualExtractedDocs = listOf(
                                    someDoc.copy(
                                        value = someDoc.value.dropLast(1),
                                        isValid = true
                                    )
                                ),
                                expectedMatchResult = true,
                            )

                            matchTest(
                                testName = "Ожидается док тайп и валидация - результат: валидация не совпадает - не соответствует результату",
                                expectedResult = baseExpectedResult.copy(
                                    expected = listOf(
                                        someDoc.copy(value = "", isValidSetup = true)
                                    )
                                ),
                                actualExtractedDocs = listOf(
                                    someDoc.copy(
                                        isValid = !someDoc.isValid
                                    )
                                ),
                                expectedMatchResult = false,
                            )

                            matchTest(
                                testName = "Ожидается док тайп, номер и валидация - все совпадает - соответствует результату",
                                expectedResult = baseExpectedResult.copy(
                                    expected = listOf(
                                        someDoc.copy(value = "0123456789", isValidSetup = true, isValid = true)
                                    )
                                ),
                                actualExtractedDocs = listOf(
                                    someDoc.copy(
                                        value = "0123456789",
                                        isValid = true,
                                    )
                                ),
                                expectedMatchResult = true,
                            )

                            matchTest(
                                testName = "Ожидается док тайп, номер и валидация - валидация не совпадает - не соответствует результату",
                                expectedResult = baseExpectedResult.copy(
                                    expected = listOf(
                                        someDoc.copy(value = "0123456789", isValidSetup = true, isValid = true)
                                    )
                                ),
                                actualExtractedDocs = listOf(
                                    someDoc.copy(
                                        value = "0123456789",
                                        isValid = false,
                                    )
                                ),
                                expectedMatchResult = false,
                            )
                        }
                    }
                }
            }
        }
    }
}
