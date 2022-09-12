package codes.spectrum.conf2022

import codes.spectrum.conf2022.engine.TestDesc
import codes.spectrum.conf2022.input.RandomSuccessfulParser
import codes.spectrum.conf2022.output.ExpectedResult
import io.kotest.core.spec.style.FunSpec
import io.kotest.inspectors.forAll
import kotlinx.coroutines.launch
import java.io.File

class LocalTest : FunSpec() {
    private val inputFileName = "local.csv"
    private val testDescFile = File(inputFileName)
    private val docParser = RandomSuccessfulParser()

    init {
        context("локальные тесты") {
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