package codes.spectrum.conf2022.input

import codes.spectrum.conf2022.output.ExtractedDocument

/**
 * Собственно интерфейс парсера входных строк
 * Именно реализацию этого интерфейса должны реализовать участники
 * */
interface IDocParser {
    /**
     * Спарсить входную строку в набор документов
     * */
    fun parse(input: String): List<ExtractedDocument>
}