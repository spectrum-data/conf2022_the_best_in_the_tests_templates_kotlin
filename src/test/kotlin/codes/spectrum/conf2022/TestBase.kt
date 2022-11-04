package codes.spectrum.conf2022

import codes.spectrum.conf2022.engine.TestDesc
import codes.spectrum.conf2022.engine.TestDesc.Companion.extractTestDescriptions
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
    var isBasePass: Boolean,

    /**
     * Результаты запуска своих тестов
     * */
    val localResults: MutableList<TestResult>,

    /**
     * Результаты запуска общих тестов
     * */
    val mainResults: MutableList<TestResult>
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

    /** Набор описаний базовых тестов */
    private lateinit var baseTests: List<TestDesc>

    /** Набор описаний локальных тестов */
    private lateinit var localTests: List<TestDesc>

    /** Набор описаний общих тестов */
    private lateinit var mainTests: List<TestDesc>

    /**
     * Логин участника
     * */
    val MY_LOGIN: String by lazy { "harisov" }

    /**
     * Экземпляр парсера, который необходимо реализовать участникам
     * */
    val docParser = object : IDocParser {
        override fun parse(input: String): List<ExtractedDocument> {
            return emptyList()
        }
    }

    init {
        validateFiles()

        baseTests = getBaseFile()?.extractTestDescriptions() ?: emptyList()
        localTests = getLocalFile()?.extractTestDescriptions() ?: emptyList()
        mainTests = getMainFile()?.extractTestDescriptions() ?: emptyList()

        val statistics =
            TestStatistics(isBasePass = true, localResults = mutableListOf(), mainResults = mutableListOf())

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
    }
}