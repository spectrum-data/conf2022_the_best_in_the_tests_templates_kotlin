package codes.spectrum.conf2022.input

import codes.spectrum.conf2022.base.TBITConfig
import java.io.BufferedReader
import java.io.File

class TestDescReader(val config: TBITConfig) {

    private val testDescSourceFile = File(config.testInputFilePath)

    init {
        require(testDescSourceFile.exists()) {
            "Файл с исходными данными для тестов не создан. Path: ${testDescSourceFile.absolutePath}"
        }
    }

    fun <T> reading(block: (Sequence<TestDesc>) -> T) : T {
        return testDescSourceFile.bufferedReader(bufferSize = 64 * 1024).use {reader ->
            if(config.withCsvHeader) {
                reader.readLine()
            }

            block(
                sequence {
                    var currentTestDesc = reader.readTestDesc()

                    while (currentTestDesc != null) {
                        yield(currentTestDesc)

                        currentTestDesc = reader.readTestDesc()
                    }
                }
            )
        }
    }

    private fun BufferedReader.readTestDesc() : TestDesc? {
        return readLine()?.let { line -> TestDesc.parse(line, config) }
    }
}