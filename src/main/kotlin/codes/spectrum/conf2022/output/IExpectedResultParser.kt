package codes.spectrum.conf2022.output

/**
 * Парсер входной строки ожидаемого результата в структуру
 * */
interface IExpectedResultParser {

    fun parse(inputString : String) : ExpectedResult
}

class ExpectedResultParser() : IExpectedResultParser{
    override fun parse(inputString: String): ExpectedResult {
        TODO("Not yet implemented")
    }
}
