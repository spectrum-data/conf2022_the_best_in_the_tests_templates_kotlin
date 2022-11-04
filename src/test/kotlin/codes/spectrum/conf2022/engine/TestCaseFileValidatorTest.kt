package codes.spectrum.conf2022.engine

import codes.spectrum.conf2022.engine.TestDesc.Companion.DEFAULT_HEADER
import codes.spectrum.conf2022.output.ExpectedResult
import io.kotest.core.spec.style.FunSpec
import io.kotest.core.spec.style.scopes.FunSpecContainerContext
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.launch
import java.io.File
import kotlin.random.Random
import codes.spectrum.conf2022.engine.TestDesc.Companion.DEFAULT_COLUMN_DELIMITER as DEFAULT_DELIMITER


class TestCaseFileValidatorTest : FunSpec() {
    private fun createTempFileWithLines(lines: List<String>): File {
        val name = Random(1).nextInt().toString()

        return File.createTempFile(/* prefix = */ name, /* suffix = */ null).also {
            it.writer().use { writer ->
                lines.forEach { line ->
                    writer.appendLine(line)
                }
            }
        }
    }

    private fun FunSpecContainerContext.testValidate(
        testName: String,
        actualFileLines: List<String>,
        expectedValidateResult: TestDesc.TestDescFileValidateResult
    ): Unit {
        launch {
            test(testName) {
                val testCasesFile = createTempFileWithLines(actualFileLines)

                val validateResult = TestDesc.validate(testCasesFile)

                validateResult shouldBe expectedValidateResult
            }
        }
    }

    init {
        context("Дефолтные хедер и разделитель") {
            val defaultHeaderDelimiterCount = DEFAULT_HEADER.split(DEFAULT_DELIMITER).size - 1
            val correctInputString = "Паспорт РФ 1234567890==PASSPORT_RF:1234567890"

            testValidate(
                testName = "файл содержит только ожидаемый - файл валиден",
                actualFileLines = listOf(DEFAULT_HEADER),
                expectedValidateResult = TestDesc.TestDescFileValidateResult(isValid = true)
            )

            testValidate(
                testName = "файл содержит только не ожидаемый заголовок - файл не валиден, сообщение",
                actualFileLines = listOf("someHeader"),
                expectedValidateResult = TestDesc.TestDescFileValidateResult(
                    isValid = false,
                    errorMessages = listOf("Файл с описанием тест-кейсов не содержит ожидаемый заголовок '$DEFAULT_HEADER'")
                )
            )

            testValidate(
                testName = "количество разделителей в каждой строчке равно количеству в заголовке - файл валиден",
                actualFileLines = listOf(
                    DEFAULT_HEADER,
                    listOf(
                        "someAuthor",
                        "1",
                        correctInputString,
                        "true",
                        "someComment",
                        ""
                    ).joinToString(DEFAULT_DELIMITER),
                ),
                expectedValidateResult = TestDesc.TestDescFileValidateResult(
                    isValid = true,
                )
            )

            val incorrectDelimiterCountLine = listOf(
                "someAuthor",
                "1",
                correctInputString,
                "true",
                "someComment",
                ""
            ).joinToString(DEFAULT_DELIMITER) + DEFAULT_DELIMITER

            testValidate(
                testName = "количество разделителей не равно количеству в заголовке - не валиден, сообщение с указанием номера строки",
                actualFileLines = listOf(
                    DEFAULT_HEADER,                                                             // 1
                    incorrectDelimiterCountLine,                                                // 2
                ),
                expectedValidateResult = TestDesc.TestDescFileValidateResult(
                    isValid = false,
                    errorMessages = listOf(
                        "В строке с номером 2 не верное количество разделителей (${defaultHeaderDelimiterCount + 1}), должно быть $defaultHeaderDelimiterCount: $incorrectDelimiterCountLine"
                    )
                )
            )

            testValidate(
                testName = "входная строка не соответствует не соответствует структуре ${ExpectedResult.INPUT_STRUCTURE_REGEX} - не валиден",
                actualFileLines = listOf(
                    DEFAULT_HEADER,
                    listOf(
                        "someAuthor",
                        "1",
                        "incorrect_input_string==?unknown doc",
                        "true",
                        "someComment",
                        ""
                    ).joinToString(DEFAULT_DELIMITER)
                ),
                expectedValidateResult = TestDesc.TestDescFileValidateResult(
                    isValid = false,
                    errorMessages = listOf(
                        "В строке с номером 2 входная строка не соответствует необходимой структуре - ${ExpectedResult.INPUT_STRUCTURE_REGEX}"
                    )
                )
            )

            testValidate(
                testName = "Не не парсится номер теста - не валиден, сообщение с указанием номера некорректной строки",
                actualFileLines = listOf(
                    DEFAULT_HEADER,
                    listOf(
                        "someAuthor",
                        "someIncorrectNumber",
                        correctInputString,
                        "true",
                        "someComment",
                        ""
                    ).joinToString(DEFAULT_DELIMITER)
                ),
                expectedValidateResult = TestDesc.TestDescFileValidateResult(
                    isValid = false,
                    errorMessages = listOf(
                        "В строке с номером 2 сегмент с номеров некорректно заполнен: someIncorrectNumber"
                    )
                )
            )

            testValidate(
                testName = "Не не парсится disable флаг теста - не валиден, сообщение с указанием номера некорректной строки",
                actualFileLines = listOf(
                    DEFAULT_HEADER,
                    listOf(
                        "someAuthor",
                        "1",
                        correctInputString,
                        "someIncorrectBooleanValue",
                        "someComment",
                        ""
                    ).joinToString(DEFAULT_DELIMITER)
                ),
                expectedValidateResult = TestDesc.TestDescFileValidateResult(
                    isValid = false,
                    errorMessages = listOf(
                        "В строке с номером 2 сегмент isDisabled некорректно заполнен: someIncorrectBooleanValue"
                    )
                )
            )

            testValidate(
                testName = "совпадает связка author + number - не валиден, сообщение с указанием номера некорректной строки",
                actualFileLines = listOf(
                    DEFAULT_HEADER,

                    listOf(
                        "someAuthor",
                        "1",
                        correctInputString,
                        "true",
                        "someComment",
                        ""
                    ).joinToString(DEFAULT_DELIMITER),

                    listOf(
                        "someAuthor",
                        "1",
                        correctInputString,
                        "false",
                        "anotherComment",
                        ""
                    ).joinToString(DEFAULT_DELIMITER),
                ),
                expectedValidateResult = TestDesc.TestDescFileValidateResult(
                    isValid = false,
                    errorMessages = listOf(
                        "В строках с номерами 2 и 3 совпадает связка author+number"
                    )
                )
            )
        }
    }
}
