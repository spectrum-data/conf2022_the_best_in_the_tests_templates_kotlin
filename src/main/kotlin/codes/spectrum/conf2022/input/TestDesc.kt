package codes.spectrum.conf2022.input

import codes.spectrum.conf2022.base.TBITConfig

/**
 * author \/ number \/ stringToProcessed \/ isDisabled \/ commentOnFailure
 * harisov \/ 1 \/ паспорт Харисов Д.И. 1009 123848 \/ false \/ Не удалось определить корректный паспорт ФЛ
 * harisov \/ 2 \/ Паспорт Харисов Д.И. 10090 123848 \/ false \/ Не удалось определить некорректный паспорт ФЛ
 *
 * */

data class TestDesc(
    val author: String = "Default",
    val number: Int = -1,
    val stringToProcessed: String = "",
    val isDisabled: Boolean = false,
    val commentOnFailure: String = "Fail"
) {

    fun validate(): Unit {

    }

    companion object {
        //TODO("Не красиво")
        fun parse(line: String, config: TBITConfig): TestDesc {
            val splitLine = line.split(config.separator)

            return TestDesc(
                author = splitLine[0],
                number = splitLine[1].toInt(),
                stringToProcessed = splitLine[2],
                isDisabled = splitLine[3].toBoolean(),
                commentOnFailure = splitLine[4],
            )
        }
    }
}
