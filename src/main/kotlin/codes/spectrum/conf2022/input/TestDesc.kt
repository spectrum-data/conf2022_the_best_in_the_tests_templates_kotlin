package codes.spectrum.conf2022.input

import java.time.Instant

/**
 * TestDesc
 * Описание тестов.
 * Входной файл должен выглядеть так:
 *
 *author |input                            |stringToProcessed		|isDisabled	|commentOnFailure                                		|publishTime
 *harisov|паспорт Харисов Д.И. 1008 123848 |==PASSPORT_RF:1009123848|false     	|Не удалось определить паспорт РФ ФЛ  |2022-01-01T00:12:28.642595Z
 *harisov|Паспорт Харисов Д.И. 10090 123848|=?PASSPORT_RF:1009123848|false     	|Не удалось определить паспорт РФ ФЛ|2022-01-01T00:22:28.642595Z
 *
 * Или в упрощенном формате так:
 * паспорт Харисов Д.И. 1008 123848 -> PASSPORT_RF:1009123848 #Не удалось определить паспорт РФ ФЛ
 * Паспорт Харисов Д.И. 10090 123848 -> =?PASSPORT_RF:1009123848 #Не удалось определить паспорт РФ ФЛ
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
     * Техническое поле, необходимое при сведении
     * При работе участников - не используется
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