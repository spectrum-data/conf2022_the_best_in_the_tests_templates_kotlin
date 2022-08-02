import codes.spectrum.conf2022.base.DocType
import codes.spectrum.conf2022.outup.*
import io.kotest.core.spec.style.FunSpec
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

class TestForGeneratingJson(args: Array<String> ) : FunSpec() {
    init {
        val jsonService = Json {
            encodeDefaults = true
            prettyPrint = true
        }

        test("Генерация json") {
            val testResultDesc = TestResultDesc(
                id = "harisov_1",
                entryConditional = EntryConditional.EXACTLY,
                orderConditional = OrderConditional.DOES_NOT_MATTER,
                result = listOf(
                    AnswerDesc(
                        docType = DocType.PASSPORT,
                        assuranceRate = AssuranceRate.FACT,
                        isValid = true,
                        value = "1009123848"
                    )
                )
            )


            println(jsonService.encodeToString(testResultDesc))
        }
    }
}