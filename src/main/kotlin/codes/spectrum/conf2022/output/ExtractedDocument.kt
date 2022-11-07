package codes.spectrum.conf2022.output

import codes.spectrum.conf2022.doc_type.DocType
import kotlinx.serialization.Serializable

/**
 * Описание извлеченного документа
 * */
@Serializable
data class ExtractedDocument(
    val docType: DocType = DocType.UNDEFINED,
    val isValidSetup: Boolean = false,
    val isValid: Boolean = false,
    val value: String = ""
) {
    /**
     * Проверяет, что переданный документ подходит под данный паттерн
     * */
    fun match(comparedAnswer: ExtractedDocument): Boolean {
        val doTypesEqual = docType == comparedAnswer.docType

        val isNeedToCompareNumber = value.isNotBlank()
        val isNeedToCompareValidation = isValidSetup

        return doTypesEqual
                && (!isNeedToCompareNumber || value == comparedAnswer.value)
                && (!isNeedToCompareValidation || isValid == comparedAnswer.isValid)
    }

    /**
     * Проверяет, что если проверяется значение - оно должно быть нормализовано
     * */
    fun isNormal(): Boolean = docType.normaliseRegex.matches(value)
}

