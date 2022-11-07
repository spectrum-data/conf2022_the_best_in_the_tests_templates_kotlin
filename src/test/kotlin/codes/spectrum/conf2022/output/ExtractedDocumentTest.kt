package codes.spectrum.conf2022.output

import codes.spectrum.conf2022.doc_type.DocType
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue

/**
 * Тесты на сравнение документов
 * */
class ExtractedDocumentTest : FunSpec() {
    init {
        context("Установлен только doctype") {
            val onlyDocType =
                ExtractedDocument(docType = DocType.PASSPORT_RF, value = "", isValidSetup = false, isValid = false)

            test("Совпадает DocType - содержит информацию о валидации и номере - подходит") {
                onlyDocType.match(
                    ExtractedDocument(
                        docType = onlyDocType.docType,
                        value = "0123456789",
                        isValid = true
                    )
                ).shouldBeTrue()
            }

            test("Не совпадает DocType - не подходит") {
                onlyDocType.match(onlyDocType.copy(docType = DocType.INN_UL)).shouldBeFalse()
            }
        }

        context("Установлен только doctype и номер") {
            val docTypeAndNumber =
                ExtractedDocument(
                    docType = DocType.PASSPORT_RF,
                    value = "1234567890",
                    isValidSetup = false,
                    isValid = false
                )

            test("Совпадает DocType и номер, валидация не совпадает - подходит") {
                docTypeAndNumber.match(
                    ExtractedDocument(
                        docType = docTypeAndNumber.docType,
                        value = docTypeAndNumber.value,
                        isValid = true
                    )
                ).shouldBeTrue()
            }

            test("Совпадает DocType - не совпадает номер - не подходит") {
                docTypeAndNumber.match(
                    ExtractedDocument(
                        docType = docTypeAndNumber.docType,
                        value = docTypeAndNumber.value.dropLast(1),
                    )
                ).shouldBeFalse()
            }
        }

        context("Установлен только doctype и валидация") {
            val docTypeAndValidation =
                ExtractedDocument(
                    docType = DocType.PASSPORT_RF,
                    value = "",
                    isValidSetup = true,
                    isValid = true
                )

            test("Совпадает DocType и валидация, номер не совпадает - подходит") {
                docTypeAndValidation.match(
                    ExtractedDocument(
                        docType = docTypeAndValidation.docType,
                        value = "1234567890",
                        isValid = true
                    )
                ).shouldBeTrue()
            }

            test("Совпадает DocType, валидация не совпадает - не подходит") {
                docTypeAndValidation.match(
                    ExtractedDocument(
                        docType = docTypeAndValidation.docType,
                        isValid = !docTypeAndValidation.isValid,
                    )
                ).shouldBeFalse()
            }
        }

        context("Установлен DocType, номер и валидация") {
            val typeNumberValidation = ExtractedDocument(
                docType = DocType.PASSPORT_RF,
                value = "1234567890",
                isValidSetup = true,
                isValid = true
            )

            test("Совпадает DocType, номер и валидация - подходит") {
                typeNumberValidation.match(
                    ExtractedDocument(
                        docType = typeNumberValidation.docType,
                        value = typeNumberValidation.value,
                        isValid = typeNumberValidation.isValid,
                    )
                ).shouldBeTrue()
            }

            test("Совпадает DocType, номер - не совпадает валидация - не подходит") {
                typeNumberValidation.match(
                    ExtractedDocument(
                        docType = typeNumberValidation.docType,
                        value = typeNumberValidation.value,
                        isValid = !typeNumberValidation.isValid,
                    )
                ).shouldBeFalse()
            }
        }

    }
}
