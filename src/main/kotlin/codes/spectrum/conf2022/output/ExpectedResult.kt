package codes.spectrum.conf2022.output

import codes.spectrum.conf2022.base.doc_type.DocType
import kotlinx.serialization.Serializable

/**
 * Описание ожидаемого результата :
 *
 * вход:
 * Паспорт РФ 01 23 456789 == PASSPORT_RF:01234567890
 *
 * выход:
 * isExactly = true, isOrderRequired = true,
 * result = listOf(Document(PASSPORT_RF, "01234567890"))
 */

/**
 * Описание ожидаемого результата парсинга входной строки
 * */
@Serializable
data class ExpectedResult private constructor(
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
    val result: List<ExtractedDocument> = emptyList()
) {
    companion object {
        /**
         * Разделитель при указании нескольких документов
         *
         * 1234567890 =? PASSPORT_RF:1234567890[EXPECTED_DOCUMENTS_SEPARATOR]INN_UL:1234567890
         * */
        const val EXPECTED_DOCUMENTS_SEPARATOR = ","

        fun parse(input: String): ExpectedResult {
            val parsedByRegex = INPUT_STRUCTURE_REGEX.toRegex().matchEntire(input)

            check(parsedByRegex != null && parsedByRegex.groupValues.count() == 4) {
                "Входная строка '$input' не соответствует структуре '$INPUT_STRUCTURE_REGEX'"
            }

            val filledConstraints = createAndFillConstraints(parsedByRegex.groupValues[2])
            val parseExpectedDocs = parseExpectedDocs(parsedByRegex.groupValues[3])

            return filledConstraints.copy(result = parseExpectedDocs)
        }

        private fun parseExpectedDocs(input: String): List<ExtractedDocument> {
            return input.split(EXPECTED_DOCUMENTS_SEPARATOR).map { expectedDocDesc ->
                expectedDocDesc.split(":")
                    .let { ExtractedDocument(docType = DocType.valueOf(it[0]), value = it.getOrElse(1) { "" }) }
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
                else -> error("не ожиданное обозначение ограничений в описании теста - $constraints")
            }

            return ExpectedResult(isExactly = isExactly, isOrderRequired = isOrderRequired)
        }

        private const val INPUT_STRUCTURE_REGEX = "^([\\s\\S]+?)(\\b[=?~]{2}\\b)([\\s\\S]+?)\$"
    }
}
