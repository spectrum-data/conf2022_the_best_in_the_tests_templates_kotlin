package codes.spectrum.conf2022.output

import codes.spectrum.conf2022.doc_type.DocType
import kotlinx.serialization.Serializable

/**
 * Описание извлеченного документа
 * */
@Serializable
data class ExtractedDocument(
    val docType: DocType = DocType.UNDEFINED,
    val isValid: Boolean = true,
    val value: String = ""
) {
    override fun equals(other: Any?): Boolean {
        if (other is ExtractedDocument) {
            return internalEqual(other as ExtractedDocument)
        }

        return super.equals(other)
    }

    /**
     * Проверяет, что если проверяется значение - оно должно быть нормализовано
     * */
    fun isNormal(): Boolean = docType.normaliseRegex.matches(value)

    private fun internalEqual(comparedAnswer: ExtractedDocument): Boolean {
        return comparedAnswer.docType == docType
                && comparedAnswer.value == value
    }
}

