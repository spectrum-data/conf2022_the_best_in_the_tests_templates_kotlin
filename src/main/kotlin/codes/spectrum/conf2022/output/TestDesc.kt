package codes.spectrum.conf2022.output

/**
 * author \/ number \/ stringToProcessed \/ isDisabled \/ commentOnFailure
 * harisov \/ 1 \/ паспорт Харисов Д.И. 1009 123848==PASSPORT:1009123848 \/ false \/ Не удалось определить корректный паспорт ФЛ
 * harisov \/ 2 \/ Паспорт Харисов Д.И. 10090 123848=?PASSPORT:1009123848 \/ false \/ Не удалось определить некорректный паспорт ФЛ
 *
 * */

data class TestDesc(
    val author: String = "Default",
    val number: Int = -1,
    val stringToProcessed: String = "",
    val isDisabled: Boolean = false,
    val commentOnFailure: String = "Fail"
)
