package codes.spectrum.conf2022.engine

import codes.spectrum.conf2022.output.ExpectedResult
import java.io.File

/**
 * Описание тестов. Входной файл должен выглядеть так:
 * author | number | stringToProcessed | isDisabled | commentOnFailure | publishTime
 * harisov | 1 | паспорт Харисов Д.И. 1009 123848==PASSPORT:1009123848 | false | Не удалось определить корректный паспорт ФЛ
 * harisov | 2 | Паспорт Харисов Д.И. 10090 123848=?PASSPORT:1009123848 | false | Не удалось определить некорректный паспорт ФЛ
 *
 * */

data class TestDesc(
    val author: String = "Default",
    val number: Int = -1,
    val stringToProcessed: String = "",
    val isDisabled: Boolean = false,
    val commentOnFailure: String = "Fail"
) {
    /**
     * Результат валидации входного файла.
     * */
    data class TestDescFileValidateResult(
        val isValid: Boolean,
        val errorMessages: List<String> = emptyList()
    )

    companion object {
        const val DEFAULT_HEADER = "author|number|stringToProcessed|isDisabled|commentOnFailure|publishTime"
        const val DEFAULT_COLUMN_DELIMITER = "|"

        private val expectedDelimiterCount: Int = DEFAULT_HEADER.split(DEFAULT_COLUMN_DELIMITER).size - 1

        /**
         * Валидирует входной файл:
         *  1) наличие хедера
         *  2) кол-во разделителей в каждой строке = кол-ву разделителей в хедере
         *  3) тип полей number и isDisabled
         *  4) что идентификатор теста (author + number) уникальный
         * */
        fun validate(fileToValidate: File): TestDescFileValidateResult {
            val errorMessages = mutableListOf<String>()
            val testIdToLineNumber = mutableMapOf<String, Int>()

            fun validateHeader(header: String): List<String> {
                val headersErrors = mutableListOf<String>()

                val headerWithoutSpaces = header.replace("\\s".toRegex(), "")

                if (headerWithoutSpaces != DEFAULT_HEADER) headersErrors.add("Файл с описанием тест-кейсов не содержит ожидаемый заголовок '$DEFAULT_HEADER'")

                return headersErrors
            }

            fun validateLine(line: String, lineIndex: Int): List<String> {
                val splitLine = line.split(DEFAULT_COLUMN_DELIMITER)
                val delimiterCount = splitLine.size - 1

                val lineNumber = lineIndex + 1

                return buildList {
                    if (delimiterCount != expectedDelimiterCount) {
                        add("В строке с номером $lineNumber не верное количество разделителей ($delimiterCount), должно быть $expectedDelimiterCount: $line")
                    } else {
                        val testId = "${splitLine[0]}${splitLine[1]}"

                        if (testIdToLineNumber.containsKey(testId)) {
                            add(
                                "В строках с номерами ${testIdToLineNumber[testId]} и $lineNumber совпадает связка author+number"
                            )
                        } else {
                            testIdToLineNumber[testId] = lineNumber
                        }

                        if (!ExpectedResult.INPUT_STRUCTURE_REGEX.toRegex().matches(splitLine[2])) {
                            add("В строке с номером $lineNumber входная строка не соответствует необходимой структуре - ${ExpectedResult.INPUT_STRUCTURE_REGEX}")

                        }

                        if (!splitLine[1].trim().all { it.isDigit() })
                            add("В строке с номером $lineNumber сегмент с номеров некорректно заполнен: ${splitLine[1]}")

                        if (splitLine[3].trim().lowercase().toBooleanStrictOrNull() == null)
                            add("В строке с номером $lineNumber сегмент isDisabled некорректно заполнен: ${splitLine[3]}")
                    }
                }
            }

            fileToValidate.reader().useLines {
                it.forEachIndexed { index, line ->
                    try {
                        val trimLine = line.trim()

                        if (index == 0) errorMessages.addAll(validateHeader(trimLine))
                        else if (trimLine.isNotBlank()) errorMessages.addAll(validateLine(trimLine, index))
                    } catch (t: Throwable) {
                        errorMessages.add("При обработке строки с номером $index произошла ошибка: ${t.message}")
                    }
                }
            }

            return TestDescFileValidateResult(
                isValid = errorMessages.isEmpty(),
                errorMessages = errorMessages
            )
        }

        /**
         * Парсит входной файл в список описаний тестов
         * */
        fun parseFromFile(file: File): List<TestDesc> {
            val result = mutableListOf<TestDesc>()

            file.reader().useLines {
                it.drop(1).forEach { line ->
                    result.add(parseLine(line))
                }
            }

            return result
        }

        private fun parseLine(line: String): TestDesc {
            return line.split(DEFAULT_COLUMN_DELIMITER).map { it.trim() }.let { splitLine ->
                TestDesc(
                    author = splitLine[0],
                    number = splitLine[1].toInt(),
                    stringToProcessed = splitLine[2],
                    isDisabled = splitLine[3].toBoolean(),
                    commentOnFailure = splitLine[4]
                )
            }
        }
    }
}
