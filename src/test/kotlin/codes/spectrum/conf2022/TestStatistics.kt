package codes.spectrum.conf2022

/**
 * Статистика запуска тестов
 * */
data class TestStatistics(
    /**
     * Логин того, кто запускает тесты
     * */
    val ownerLogin: String,

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
