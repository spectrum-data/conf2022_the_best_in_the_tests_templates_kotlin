package codes.spectrum.conf2022

import java.io.File

/**
 * Запуск только БАЗОВЫХ и ЛОКАЛЬНЫХ тестов участника
 * Файл с базовыми тестами - base.csv
 * Файл с локальными тестами - local.csv
 * */
class LocalTest : TestBase(
    filesToProcess = listOf(
        File(PROJECT_ROOT_DIR, BASE_TEST_FILE_NAME),
        File(PROJECT_ROOT_DIR, LOCAL_TEST_FILE_NAME),
    ),
    enabledByDefault = false
) {
}
