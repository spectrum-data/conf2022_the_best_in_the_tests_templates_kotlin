package codes.spectrum.conf2022.output

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
        fun parse(input: String): ExpectedResult {
            TODO()
        }
    }
}
