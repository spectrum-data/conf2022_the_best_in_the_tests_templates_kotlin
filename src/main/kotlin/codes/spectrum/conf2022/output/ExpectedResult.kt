package codes.spectrum.conf2022.output

import kotlinx.serialization.Serializable

/**
 * Формат входной строки:
 * Паспорт РФ 01 23 456789 = [PASSPORT:01234567890]
 * 0123456789 = [PASSPORT:01234567890;INN_UL:0123456789]
 */

/**
 * Описание ожидаемого результата парсинга входной строки
 * */
@Serializable
data class ExpectedResult(
    /**
     * Ограничение на вхождение результатов в итоговую выборку
     * */
    val entryConstraint: EntryConstraint = EntryConstraint.CONTAINS,

    /**
     * Ограничение на порядок расположения результатов в выборке
     * */
    val orderConditional: OrderConditional = OrderConditional.DOES_NOT_MATTER,

    /**
     * Набор извлеченных документов
     * */
    val result: List<ExtractedDocument> = emptyList()
) {

    fun isExactly(): Boolean {
        return entryConstraint == EntryConstraint.EXACTLY
    }

    fun isContains(): Boolean {
        return entryConstraint == EntryConstraint.CONTAINS
    }

    fun isOrder(): Boolean {
        return orderConditional == OrderConditional.SAME_ORDER
    }
}
