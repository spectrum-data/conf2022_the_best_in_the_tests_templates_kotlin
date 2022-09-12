package codes.spectrum.conf2022

import codes.spectrum.conf2022.engine.TestDesc
import codes.spectrum.conf2022.input.IDocParser
import codes.spectrum.conf2022.input.RandomSuccessfulParser
import codes.spectrum.conf2022.output.ExpectedResult
import codes.spectrum.conf2022.output.ExtractedDocument
import io.kotest.core.spec.Spec
import io.kotest.core.spec.style.FunSpec
import io.kotest.core.test.TestCase
import io.kotest.core.test.TestCaseOrder
import io.kotest.inspectors.forAll
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.launch
import java.io.File

class LocalTest : FunSpec() {
    val inputFileName = "local.csv"
    val testDescFile = File(inputFileName)
    val docParser = RandomSuccessfulParser()

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