package codes.spectrum.conf2022

import java.io.File

/**
 * Запуск базовых тестов
 * */
class BaseTest : TestBase(
    filesToProcess = listOf(
        File(PROJECT_ROOT_DIR, BASE_TEST_FILE_NAME)
    )
) {
}