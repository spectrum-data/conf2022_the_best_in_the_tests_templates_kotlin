package codes.spectrum.conf2022.outup

import codes.spectrum.conf2022.base.DocType
import kotlinx.serialization.Serializable

@Serializable
data class AnswerDesc(
    val docType: DocType = DocType.UNDEFINED,
    val assuranceRate: AssuranceRate = AssuranceRate.FACT,
    val isValid: Boolean = true,
    val value: String = ""
) {
    override fun equals(other: Any?): Boolean {
        if (other is AnswerDesc) {
            return internalEqual(other as AnswerDesc)
        }

        return super.equals(other)
    }

    private fun internalEqual(comparedAnswer: AnswerDesc): Boolean {
        return comparedAnswer.docType == docType
                && comparedAnswer.assuranceRate == assuranceRate
                && comparedAnswer.value == value
    }
}

