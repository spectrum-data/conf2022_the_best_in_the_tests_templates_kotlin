package codes.spectrum.conf2022

import codes.spectrum.conf2022.engine.TestDesc
import codes.spectrum.conf2022.input.IDocParser
import codes.spectrum.conf2022.output.ExpectedResult
import codes.spectrum.conf2022.output.ExtractedDocument
import io.kotest.core.spec.style.FunSpec
import io.kotest.core.spec.style.scopes.FunSpecContainerContext
import java.io.File

/**
 * Статистика запуска тестов
 * */
data class TestStatistics(
    /**
     * Пройдены ли базовые тесты
     * */
    val isBasePass: Boolean,

    /**
     * Результаты запуска своих тестов
     * */
    val localResult: List<TestResult>,

    /**
     * Результаты запуска общих тестов
     * */
    val mainResult: List<TestResult>
)

/**
 * Результат запуска теста
 * */
data class TestResult(
    /**
     * Автор теста
     * */
    val author: String,

    /**
     * Строка для обработки
     * */
    val stringToProcessed: String,

    /**
     * Пройден ли тест
     * */
    val isPass: Boolean
)

/**
 * Базовый спек для запуска тестов. Работает с описанями тестов в формате csv-файлов.
 * */
abstract class TestBase(val filesToProcess: List<File>) : FunSpec() {
    /**
     * Статистика выполнения тестов
     * */
    lateinit var statistics: TestStatistics

    /**
     * Логин участника
     * */
    val MY_LOGIN: String by lazy { "LOKBUGS" }

    /**
     * Экземпляр парсера, который необходимо реализовать участникам
     * */
    val docParser = object : IDocParser {
        override fun parse(input: String): List<ExtractedDocument> {
            return emptyList()
        }
    }

    suspend fun FunSpecContainerContext.runTest(
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

    init {
        filesToProcess.forEach { file ->
            val validateResult = TestDesc.validate(file)

            if (!validateResult.isValid)
                error("Файл - ${file.name} не валидный. Ошибки: ${validateResult.errorMessages}")
        }

        val baseTestFile = filesToProcess.first { it.name == BASE_TEST_FILE_NAME }
        var isBasePass = true

        val localResults = mutableListOf<TestResult>()
        val mainResults = mutableListOf<TestResult>()

        context("Базовый функционал - базовые тесты") {
            val descriptions = TestDesc.parseFromFile(baseTestFile)

            descriptions.forEach { testDesc ->
                runTest(testDesc) { isMatch ->
                    if (!isMatch)
                        isBasePass = false
                }
            }
        }

        val localTestFile = filesToProcess.firstOrNull { it.name == LOCAL_TEST_FILE_NAME }

        if (localTestFile != null) {
            context("Запуск локальных тестов - файл ${localTestFile.name}") {
                val descriptions = TestDesc.parseFromFile(localTestFile)

                descriptions.forEach { testDesc ->
                    runTest(testDesc) { isMatch ->
                        localResults.add(
                            TestResult(
                                author = testDesc.author,
                                stringToProcessed = testDesc.stringToProcessed,
                                isPass = isMatch
                            )
                        )
                    }
                }
            }
        }

        val mainTestFile = filesToProcess.firstOrNull { it.name == MAIN_TEST_FILE_NAME }

        if (mainTestFile != null) {
            val descriptions = TestDesc.parseFromFile(mainTestFile)

            val localsButNotInLocalFile = descriptions.filter { it.author == MY_LOGIN }

            if (localsButNotInLocalFile.isNotEmpty()) {
                context("Запуск своих тестов, которые есть в ${mainTestFile.name}, но которых нет в ${localTestFile?.name}") {
                    localsButNotInLocalFile.forEach { testDesc ->
                        runTest(testDesc) { isMatch ->
                            localResults.add(
                                TestResult(
                                    author = testDesc.author,
                                    stringToProcessed = testDesc.stringToProcessed,
                                    isPass = isMatch
                                )
                            )
                        }
                    }
                }
            }

            val otherCompetitor = descriptions.filter { it.author != MY_LOGIN }

            otherCompetitor.groupBy { it.author }.forEach { authorToTests ->
                context("Тесты от ${authorToTests.key}") {
                    authorToTests.value.forEach { testDesc ->
                        runTest(testDesc) { isMatch ->
                            mainResults.add(
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

        statistics = TestStatistics(isBasePass = isBasePass, localResult = localResults, mainResult = mainResults)
    }

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
    }
}