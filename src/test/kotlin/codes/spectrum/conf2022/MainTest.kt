package codes.spectrum.conf2022

import codes.spectrum.conf2022.input.TestDesc.Companion.csvHeader
import java.io.File
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.concurrent.TimeUnit

/**
 * Запуск только БАЗОВЫХ, ЛОКАЛЬНЫХ тестов участника и ОБЩИХ тестов всех участников
 * Файл с базовыми тестами - base.csv
 * Файл с локальными тестами - local.csv
 * Файл с общими тестами всех участников main.csv - выкачивается из общего репозитория
 * */
class MainTest : TestBase(
    filesToProcess = listOf(
        File(PROJECT_ROOT_DIR, BASE_TEST_FILE_NAME),
        File(PROJECT_ROOT_DIR, LOCAL_TEST_FILE_NAME),
        getActualMain(),
    )
) {
    init {

    }

    companion object {
        /**
         * Выгружает из репозитория актуальное состояние файла main.csv
         * Сохраняет его во временную директорию - отдает ссылку на файл
         * */
        private fun getActualMain(): File {
            ///https://$TOKEN@raw.githubusercontent.com/<user or organization>/<repo name>/<branch>/<path to file>/<file_name>
            val token = "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"

            val request = HttpRequest
                .newBuilder()
                .GET()
                .uri(
                    URI("https://raw.githubusercontent.com/spectrum-data/conf2022_the_best_in_the_tests_templates_base/main/main.csv")
                )
                .build()

            val response = HttpClient.newHttpClient()
                .send(
                    request, HttpResponse.BodyHandlers.ofString()
                )

            var result: String

            if (response.statusCode() == 200) {
                result = response.body()
            } else {

                //TODO("ДАННЫЙ ВЫЗОВ НУЖЕН ТОЛЬКО ДЛЯ ТЕСТИРОВАНИЯ - ПОСЛЕ ПУБЛИКАЦИИ ДОЛЖЕН БЫТЬ УДАЛЕН.")
                //TODO("По токену из приватного репозитория не получается получить файл по АПИ (даже с токеном)")
                //TODO("После публикации - нет необходимости в токене")

                val processing = ProcessBuilder(
                    "curl",
                    "https://${token}@raw.githubusercontent.com/spectrum-data/conf2022_the_best_in_the_tests_templates_base/main/main.csv"
                )
                    .redirectErrorStream(true)
                    .start()

                processing.waitFor(2, TimeUnit.SECONDS)

                processing.inputStream.reader().use { result = it.readText().replaceBefore(csvHeader, "") }
            }

            return File(PROJECT_ROOT_DIR, MAIN_TEST_FILE_NAME).also {
                it.createNewFile()
                it.writeText(result)
            }
        }
    }
}