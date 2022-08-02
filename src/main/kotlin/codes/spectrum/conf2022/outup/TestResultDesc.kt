package codes.spectrum.conf2022.outup

import kotlinx.serialization.Serializable

/**
 * Входной файл
 * harisov \/ 1 \/ паспорт Харисов Д.И. 1009 123848 \/ false \/ Не удалось определить корректный паспорт ФЛ
 * harisov \/ 2 \/ Паспорт Харисов Д.И. 10090 123848 \/ false \/ Не удалось определить некорректный паспорт ФЛ
 *
 * Файл для проверки
[
    {
        "id": "harisov_1",
        "entryConditional": "EXACTLY",
        "orderConditional": "DOES_NOT_MATTER",
        "result": [
            {
            "docType": "PASSPORT",
            "assuranceRate": "FACT",
            "isValid": true,
            "value": "1009123848"
            }
        ]
    },
    {
        "id": "harisov_2",
        "entryConditional": "EXACTLY",
        "orderConditional": "DOES_NOT_MATTER",
        "result": [
        {
            "docType": "PASSPORT",
            "assuranceRate": "FACT",
            "isValid": false,
            "value": "1009123848"
        }
        ]
    }
]
 * */

@Serializable
data class TestResultDesc(
    val id: String = "",
    val entryConditional: EntryConditional = EntryConditional.CONTAINS,
    val orderConditional: OrderConditional = OrderConditional.DOES_NOT_MATTER,
    val result: List<AnswerDesc> = emptyList()
) {
    fun isExactly() : Boolean {
        return entryConditional == EntryConditional.EXACTLY
    }

    fun isContains() : Boolean {
        return entryConditional == EntryConditional.CONTAINS
    }

    fun isOrder() : Boolean {
        return orderConditional == OrderConditional.SAME_ORDER
    }
}
