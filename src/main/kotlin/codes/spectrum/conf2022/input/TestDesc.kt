package codes.spectrum.conf2022.input

import java.time.Instant

/**
 * Описатель теста
 */
data class TestDesc(
    /**
     * Автор
     */
    val author: String = "",
    /**
     * Вход
     */
    val input: String = "",
    /**
     * Ожидаемый результат (в нормализованном виде),
     * дополняется `==` если нет префикса
     */
    val expected: String = "",
    /**
     * Признак отключения (или исчез в исходниках или иная причина)
     */
    val isDisabled: Boolean = true,
    /**
     * Комментарий к тесту
     */
    val commentOnFailure: String = "",
    /**
     * Время публикации
     */
    val publishTime: Instant = Instant.MIN,
) {
    /**
     * Бизнес-ключ теста, используется для выявления уникальности
     */
    val bizKey by lazy { "$author:$input->$expected" }

    fun toCsvString(): String = listOf(
        author,
        input,
        expected,
        isDisabled.toString(),
        commentOnFailure,
        publishTime.toString()
    ).joinToString(DEFAULT_DELIMITER)

    fun toLocalString(): String = buildString {
        append(input)
        append(" -> ")
        append(expected)
        if (commentOnFailure.isNotBlank()) {
            append(" # ")
            append(commentOnFailure)
        }
    }

    companion object {
        val None = TestDesc()

        /**
         * Дефолтный разделитель
         * */
        const val DEFAULT_DELIMITER = "|"

        val csvHeader: String by lazy {
            listOf(
                "author", "input", "expected", "isDisabled", "commentOnFailure", "publishTime"
            ).joinToString(DEFAULT_DELIMITER)
        }
    }
}