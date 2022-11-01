package codes.spectrum.conf2022

import codes.spectrum.conf2022.input.IDocParser
import codes.spectrum.conf2022.output.ExtractedDocument
import io.kotest.core.spec.style.FunSpec

/**
 * Набор базовых тестов, которым должен удовлетворять парсер документов
 *
 * Базовые тесты - умение парсить русифицированное название документа
 * */
class BaseTest : FunSpec() {

    /**
     * Экземпляр парсера, который необходимо реализовать участникам
     * */
    val docParser = object : IDocParser {
        override fun parse(input: String): List<ExtractedDocument> {
            return emptyList()
        }
    }

    init {

    }
}