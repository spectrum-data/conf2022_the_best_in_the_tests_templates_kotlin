package codes.spectrum.conf2022

import codes.spectrum.conf2022.engine.TestDesc
import codes.spectrum.conf2022.input.RandomSuccessfulParser
import io.kotest.core.spec.style.FunSpec
import java.io.File

class ValidateLocal : FunSpec() {
    val inputFileName = "local.csv"
    val testDescFile = File(inputFileName)

    init {
        test("валидация входного файла") {
            val testDescValidateResult = TestDesc.validate(testDescFile)

            assert(testDescValidateResult.isValid) {
                "Входной файл ${inputFileName} не валиден:\n" +
                        testDescValidateResult.errorMessages.joinToString("\n")
            }
        }
    }
}