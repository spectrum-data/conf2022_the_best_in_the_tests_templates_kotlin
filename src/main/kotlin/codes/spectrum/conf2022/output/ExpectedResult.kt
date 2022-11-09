package codes.spectrum.conf2022.output

import codes.spectrum.conf2022.doc_type.DocType
import kotlinx.serialization.Serializable

/**
 * Описание ожидаемого результата парсинга входной строки
 * */
@Serializable
data class ExpectedResult(
    /**
     * Ограничение на вхождение результатов в итоговую выборку
     * true - исключительно ожидаемый набор и ничего кроме
     * false - ожидаемый набор содержатся в итоговой выборке, но могут быть и другие
     * */
    val isExactly: Boolean = false,

    /**
     * Ограничение на порядок расположения результатов в выборке
     * true - ожидаемый набор в указанном порядке
     * false - ожидаемый набор в любом порядке
     * */
    val isOrderRequired: Boolean = false,

    /**
     * Набор ожидаемых извлеченных документов
     * */
    val expected: List<ExtractedDocument> = emptyList()
) {

    /**
     * Проверяет набор документов на совпадение с ожидаемым результатом.
     * */
    fun match(actual: List<ExtractedDocument>): Boolean {
        val doCountsEqual = actual.count() == expected.count()

        return when {
            isExactly && isOrderRequired -> {
                doCountsEqual && expected.zip(actual).all { (expectedDoc, actualDoc) -> expectedDoc.match(actualDoc) }
            }

            isExactly && !isOrderRequired -> {
                doCountsEqual && actual.all { actualDoc -> expected.any { expectedDoc -> expectedDoc.match(actualDoc) } }
            }

            /**
             * Должно ли быть эквивалетно?
             * Expected: PASSPORT_RF, INN_FL, INN_UL
             * Actual: PASSPORT_RF, INN_UL
             * */
            !isExactly && isOrderRequired -> {
                var subsequenceIndex = 0
                val actualIterator = actual.iterator()

                while (actualIterator.hasNext() && subsequenceIndex < expected.size) {
                    if (expected[subsequenceIndex].match(actualIterator.next())) subsequenceIndex += 1
                }

                return subsequenceIndex == expected.size
            }

            !isExactly && !isOrderRequired -> expected.all { expectedDoc ->
                actual.any { actualDoc ->
                    expectedDoc.match(
                        actualDoc
                    )
                }
            }

            else -> error("Некорректной сконфигурирован ожидаемый результат: $this")
        }
    }

    companion object {
        /**
         * Разделитель при указании нескольких документов
         *
         * 1234567890 =? PASSPORT_RF:1234567890[EXPECTED_DOCUMENTS_SEPARATOR]INN_UL:1234567890
         * */
        const val EXPECTED_DOCUMENTS_SEPARATOR = ","

        fun parse(fullStringToProcessed: String): ExpectedResult {
            val splitByRegex = INPUT_STRUCTURE_REGEX.toRegex().matchEntire(fullStringToProcessed)

            check(splitByRegex != null && splitByRegex.groupValues.count() == 4) {
                "Входная строка '$fullStringToProcessed' не соответствует структуре '$INPUT_STRUCTURE_REGEX'"
            }

            val filledConstraints = createAndFillConstraints(splitByRegex.groupValues[2])
            val parseExpectedDocs = parseExpectedDocs(splitByRegex.groupValues[3])

            return filledConstraints.copy(expected = parseExpectedDocs)
        }

        private fun parseExpectedDocs(input: String): List<ExtractedDocument> {
            return input.split(EXPECTED_DOCUMENTS_SEPARATOR).map { expectedDocDesc ->
                expectedDocDesc.split(":")
                    .let {
                        val value = it.getOrElse(1) { "" }.trim()

                        val trimmedFirstPart = it[0].trim()

                        if (trimmedFirstPart.endsWith(VALID_DOC_SUFFIX)) {
                            ExtractedDocument(
                                docType = DocType.valueOf(trimmedFirstPart.dropLast(1).uppercase()),
                                isValid = true,
                                isValidSetup = true,
                                value = value,
                            )
                        } else if (trimmedFirstPart.endsWith(INVALID_DOC_SUFFIX)) {
                            ExtractedDocument(
                                docType = DocType.valueOf(trimmedFirstPart.dropLast(1).uppercase()),
                                isValid = false,
                                isValidSetup = true,
                                value = value,
                            )
                        } else {
                            ExtractedDocument(
                                docType = DocType.valueOf(trimmedFirstPart.uppercase()),
                                isValidSetup = false,
                                value = value,
                            )
                        }
                    }.also {
                        if (!it.isNormal()) {
                            error("Указанный номер - '${it.value}' - не соответствует нормализованному формату ${it.docType.normaliseRegex} для ${it.docType}")
                        }
                    }
            }
        }

        private fun createAndFillConstraints(constraints: String): ExpectedResult {
            var isExactly: Boolean? = null
            var isOrderRequired: Boolean? = null

            when (constraints) {
                "==" -> {
                    isExactly = true; isOrderRequired = true
                }

                "~=" -> {
                    isExactly = false; isOrderRequired = true
                }

                "=?" -> {
                    isExactly = true; isOrderRequired = false
                }

                "~?" -> {
                    isExactly = false; isOrderRequired = false
                }

                else -> error("неожиданное обозначение ограничений в описании теста - $constraints")
            }

            return ExpectedResult(isExactly = isExactly, isOrderRequired = isOrderRequired)
        }

        /**
         * Суфикс валидного документа документа - номер документа валиден
         * */
        const val VALID_DOC_SUFFIX = "+"

        /**
         * Суфикс не валидного документа документа - номер документа не валиден
         * */
        const val INVALID_DOC_SUFFIX = "-"

        /**
         * Регулярное выражение структуры описания теста
         * */
        const val INPUT_STRUCTURE_REGEX = "^([\\s\\S]*?[^~=?]+)(==|~=|=\\?|~\\?)([^~=?]+[\\s\\S]+?)\$"
    }
}