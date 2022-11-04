package codes.spectrum.conf2022

import java.io.File

/**
 * Запуск базовых и локальных тестов
 * */
class LocalTest : TestBase(
    filesToProcess = listOf(
        File(PROJECT_ROOT_DIR, BASE_TEST_FILE_NAME),
        File(PROJECT_ROOT_DIR, LOCAL_TEST_FILE_NAME),
    )
) {
}