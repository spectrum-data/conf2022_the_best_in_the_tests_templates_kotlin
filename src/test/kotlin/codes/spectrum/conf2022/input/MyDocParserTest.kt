package codes.spectrum.conf2022.input

import codes.spectrum.conf2022.base.doc_type.DocType
import io.kotest.core.spec.style.FunSpec
import io.kotest.inspectors.forAll
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.launch

class MyDocParserTest : FunSpec() {
    init {
        context("Однозначный документ") {
            context("Паспорт РФ") {
                listOf(
                    // Ключевое слово
                    "Паспорт РФ 0123 456789",
                    "паспорт 0123 456789",
                    "Passport 0123 456789",

                    // Разделители в серии и между серией и номером
                    "Паспорт РФ 01 23 456789",
                    "Паспорт РФ 0123456789",
                ).forAll {
                    launch {
                        test(it) {
                            val myDocParser = MyDocParser()

                            val result = myDocParser.parse(it)
                                .single()

                            result.docType shouldBe DocType.PASSPORT
                            result.value shouldBe "0123456789"
                        }
                    }
                }
            }
        }
    }
}
