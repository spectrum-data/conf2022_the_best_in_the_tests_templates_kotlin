package codes.spectrum.conf2022

import codes.spectrum.conf2022.engine.TestDesc
import codes.spectrum.conf2022.input.RandomSuccessfulParser
import codes.spectrum.conf2022.output.ExpectedResult
import io.kotest.core.spec.style.FunSpec
import io.kotest.inspectors.forAll
import kotlinx.coroutines.launch
import java.io.File

class LocalTest : FunSpec() {
    val inputFileName = "local.csv"
    val testDescFile = File(inputFileName)
    val docParser = RandomSuccessfulParser()

    init {
        context("локальные тесты") {
            val testDescValidateResult = TestDesc.validate(testDescFile)

            test("валидация входного файла") {
                assert(testDescValidateResult.isValid) {
                    "Входной файл ${inputFileName} не валиден:\n" +
                            testDescValidateResult.errorMessages.joinToString("\n")
                }
            }

            if (testDescValidateResult.isValid) {
                TestDesc.parseFromFile(testDescFile).forAll { testDesc ->

                    if (!testDesc.isDisabled) {
                        launch {
                            test("${testDesc.author} №${testDesc.number}") {
                                val expectedResult = ExpectedResult.parse(testDesc.stringToProcessed)

                                val actualResult = docParser.parse(testDesc.stringToProcessed)

                                assert(expectedResult.match(actualResult)) {
                                    "${testDesc.commentOnFailure}.\nВходная строка: ${testDesc.stringToProcessed}\nРезультат:${actualResult}"
                                }

                            }
                        }
                    }
                }
            }
        }
    }
}