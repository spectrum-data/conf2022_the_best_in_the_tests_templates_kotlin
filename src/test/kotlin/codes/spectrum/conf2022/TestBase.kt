package codes.spectrum.conf2022

import codes.spectrum.conf2022.input.TestDesc
import codes.spectrum.conf2022.input.TestDesc.Companion.extractTestDescriptions
import codes.spectrum.conf2022.input.RandomSuccessfulParser
import codes.spectrum.conf2022.output.ExpectedResult
import io.kotest.core.spec.Spec
import io.kotest.core.spec.style.FunSpec
import io.kotest.core.spec.style.scopes.FunSpecContainerContext
import java.io.File
import java.io.OutputStreamWriter

/**
 * Базовый спек для запуска тестов. Работает с описанями тестов в формате csv-файлов.
 * */
abstract class TestBase(val filesToProcess: List<File>) : FunSpec() {
    /**
     * Статистика выполнения тестов
     * */
    lateinit var statistics: TestStatistics

    /** Набор описаний базовых тестов */
    private lateinit var baseTests: List<TestDesc>

    /** Набор описаний локальных тестов */
    private lateinit var localTests: List<TestDesc>

    /** Набор описаний общих тестов */
    private lateinit var mainTests: List<TestDesc>


    // TODO("вернуть TODO здесь")
    /**
     * Логин участника
     * */
    val MY_LOGIN: String by lazy { "lokbugs" }

    /**
     * Экземпляр парсера, который необходимо реализовать участникам
     * */
    val docParser = RandomSuccessfulParser()

//        object : IDocParser {
//        override fun parse(input: String): List<ExtractedDocument> {
//            return emptyList()
//        }
//    }

    override fun afterSpec(spec: Spec) {
        makeReport()
    }

    init {
        validateFiles()

        baseTests = getBaseFile()?.extractTestDescriptions() ?: emptyList()
        localTests = getLocalFile()?.extractTestDescriptions() ?: emptyList()
        mainTests = getMainFile()?.extractTestDescriptions() ?: emptyList()

        statistics =
            TestStatistics(
                ownerLogin = MY_LOGIN,
                isBasePass = true,
                localResults = mutableListOf(),
                mainResults = mutableListOf()
            )

        context("Базовый функционал - базовые тесты") {
            baseTests.forEach { testDesc ->
                runTest(testDesc) { isMatch ->
                    if (!isMatch)
                        statistics.isBasePass = false
                }
            }
        }

        context("Запуск локальных тестов") {
            localTests.forEach { testDesc ->
                runTest(testDesc) { isMatch ->
                    statistics.localResults.add(
                        TestResult(
                            author = testDesc.author,
                            stringToProcessed = testDesc.stringToProcessed,
                            isPass = isMatch
                        )
                    )
                }
            }
        }

        mainTests.groupBy { it.author }.forEach { authorToTests ->
            // В общих могут присутствовать тесты автора - исключим те, которые уже запускали
            if (authorToTests.key.lowercase() == MY_LOGIN.lowercase()) {
                context("Запуск своих тестов, которые есть в общих, но которых нет в локальном файле") {
                    authorToTests.value.forEach { testDesc ->
                        if (testDesc.stringToProcessed.trim() !in localTests.map { it.stringToProcessed.trim() }) {
                            runTest(testDesc) { isMatch ->
                                statistics.localResults.add(
                                    TestResult(
                                        author = authorToTests.key,
                                        stringToProcessed = testDesc.stringToProcessed,
                                        isPass = isMatch
                                    )
                                )
                            }
                        }
                    }
                }
            } else {
                context("Тесты от ${authorToTests.key}") {
                    authorToTests.value.forEach { testDesc ->
                        runTest(testDesc) { isMatch ->
                            statistics.mainResults.add(
                                TestResult(
                                    author = authorToTests.key,
                                    stringToProcessed = testDesc.stringToProcessed,
                                    isPass = isMatch
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    /**
     * Запустить тест по его описанию
     * */
    private suspend fun FunSpecContainerContext.runTest(
        testDesc: TestDesc,
        matchProcess: (Boolean) -> Unit
    ) {
        if (!testDesc.isDisabled) {
            val expected = ExpectedResult.parse(testDesc.stringToProcessed)

            test("Входная строка - ${testDesc.stringToProcessed}. Ожидаемый список доков - ${expected.expected}") {
                val actual = docParser.parse(testDesc.stringToProcessed)
                val isMatch = expected.match(actual)

                matchProcess(isMatch)

                assert(isMatch) {
                    buildString {
                        appendLine(testDesc.commentOnFailure)
                        appendLine("Входная строка - ${testDesc.stringToProcessed}")
                        appendLine("Ожидаемый список доков - ${expected.expected}")
                        appendLine("Актуальный список доков - $actual")
                    }
                }
            }
        }
    }

    /**
     * Создает файл с отчетом + выводит отчет в консоль.
     * */
    private fun makeReport(): File {
        fun OutputStreamWriter.appendLineAndPrint(line: String) = appendLine(line.also { println(it) })
        fun OutputStreamWriter.appendTestResult(testResult: TestResult) {
            val splitStringToProcessed =
                ExpectedResult.INPUT_STRUCTURE_REGEX.toRegex().matchEntire(testResult.stringToProcessed)

            appendLine("|${testResult.author}|${splitStringToProcessed!!.groupValues[1]}|${splitStringToProcessed!!.groupValues[2]}${splitStringToProcessed!!.groupValues[3]}|${testResult.isPass}|")
        }

        val resultFile = File(PROJECT_ROOT_DIR, REPORT_FILE_NAME).also { it.createNewFile() }

        resultFile.writer().use { writer ->
            with(writer) {
                appendLineAndPrint("##### Owner`s login:${statistics.ownerLogin}")
                appendLineAndPrint("##### All basic tests were${if (statistics.isBasePass) "" else " NOT"} passed")
                appendLineAndPrint("")

                val ownPassedTests = statistics.localResults.filter { it.isPass }

                appendLineAndPrint("##### Your own tests: ${ownPassedTests.count()}/${statistics.localResults.count()}")
                appendLineAndPrint("##### So, ${ownPassedTests.count()} test(s) can get you points")
                appendLineAndPrint("")

                val groupedOtherMemberTests = statistics.mainResults.groupBy { it.author }

                appendLineAndPrint("##### Competitors:")
                groupedOtherMemberTests.forEach { authorToTests ->
                    appendLineAndPrint("###### ${authorToTests.key}: you passed ${authorToTests.value.count { it.isPass }}/${authorToTests.value.count()}")
                }
                appendLineAndPrint("")

                appendLine("##### FULL_INFO")
                appendLine("|author|input|expected|result|")
                appendLine("|-----|-----|-----|-----|")
                statistics.localResults.forEach { appendTestResult(it) }
                groupedOtherMemberTests.forEach { authorToTests ->
                    authorToTests.value.forEach {
                        appendTestResult(it)
                    }
                }
            }
        }

        return resultFile
    }

    /**
     * Валидирует входные файлы
     * */
    private fun validateFiles(): Unit = filesToProcess.forEach { file ->
        val validateResult = TestDesc.validate(file)

        if (!validateResult.isValid)
            error("Файл - ${file.name} не валидный. Ошибки: ${validateResult.errorMessages.joinToString("\n")}")
    }

    /**
     * Получить файл в базовыми тестами
     * */
    private fun getBaseFile(): File? = getFileByName(BASE_TEST_FILE_NAME)

    /**
     * Получить файл с локальными тестами
     * */
    private fun getLocalFile(): File? = getFileByName(LOCAL_TEST_FILE_NAME)

    /**
     * Получить файл с общими тестами
     * */
    private fun getMainFile(): File? = getFileByName(MAIN_TEST_FILE_NAME)

    /**
     * Получить файл по названию из списка файлов на обработку
     * */
    private fun getFileByName(name: String): File? =
        filesToProcess.firstOrNull { it.name.lowercase() == name.lowercase() }

    companion object {

        /**
         * Рутовая директория проекта
         * */
        val PROJECT_ROOT_DIR = File(System.getProperty("user.dir"))

        /**
         * Название файла с базовыми тестами
         * */
        val BASE_TEST_FILE_NAME = "base.csv"


        /**
         * Название файла с локальными тестами
         * */
        val LOCAL_TEST_FILE_NAME = "local.csv"


        /**
         * Название файла с общими тестами
         * */
        val MAIN_TEST_FILE_NAME = "main.csv"

        /**
         * Название файла с отчетом
         * */
        val REPORT_FILE_NAME = "report.md"
    }
}