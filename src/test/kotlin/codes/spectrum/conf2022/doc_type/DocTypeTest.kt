package codes.spectrum.conf2022.doc_type

import codes.spectrum.conf2022.output.ExtractedDocument
import io.kotest.core.spec.style.FunSpec
import io.kotest.core.spec.style.scopes.FunSpecContainerContext
import io.kotest.matchers.shouldBe

class DocTypeTest : FunSpec() {
    suspend fun FunSpecContainerContext.testNormaliseRegex(testName: String, doc: ExtractedDocument, isMatch: Boolean) {
        test(testName) {
            doc.isNormal() shouldBe isMatch
        }
    }

    init {
        context("Паспорт РФ") {
            val passportRf = ExtractedDocument(docType = DocType.PASSPORT_RF)

            testNormaliseRegex(
                testName = "десять цифр без пробела - валиден",
                doc = passportRf.copy(value = "0123456789"),
                isMatch = true
            )

            testNormaliseRegex(
                testName = "десять цифр, содержит пробел - не валиден",
                doc = passportRf.copy(value = "0123 456789"),
                isMatch = false
            )

            testNormaliseRegex(
                testName = "девять цифр, не содержит пробел - не валиден",
                doc = passportRf.copy(value = "123456789"),
                isMatch = false
            )

            testNormaliseRegex(
                testName = "одиннадцать цифр, не содержит пробел - не валиден",
                doc = passportRf.copy(value = "01234567891"),
                isMatch = false
            )
        }

        context("Водительское удостоверение") {
            val dl = ExtractedDocument(docType = DocType.DRIVER_LICENSE)

            testNormaliseRegex(
                testName = "десять цифр без пробела - валиден",
                doc = dl.copy(value = "0123456789"),
                isMatch = true
            )

            testNormaliseRegex(
                testName = "десять цифр, содержит пробел - не валиден",
                doc = dl.copy(value = "0123 456789"),
                isMatch = false
            )

            testNormaliseRegex(
                testName = "девять цифр, не содержит пробел - не валиден",
                doc = dl.copy(value = "123456789"),
                isMatch = false
            )

            testNormaliseRegex(
                testName = "одиннадцать цифр, не содержит пробел - не валиден",
                doc = dl.copy(value = "01234567891"),
                isMatch = false
            )
        }

        context("Идентификационный номер транспортного средства") {
            val vin = ExtractedDocument(docType = DocType.VIN)

            testNormaliseRegex(
                testName = "семнадцать цифр без пробела - валиден",
                doc = vin.copy(value = "12345678901234567"),
                isMatch = true
            )

            testNormaliseRegex(
                testName = "семнадцать заглавных латинских букв без пробела - валиден",
                doc = vin.copy(value = "ABCDEFGHIJKLMNOPQ".also { it.length shouldBe 17 }),
                isMatch = true
            )

            testNormaliseRegex(
                testName = "семнадцать строчных латинских букв без пробела - не валиден",
                doc = vin.copy(value = "abcdefghijklmnopq".also { it.length shouldBe 17 }),
                isMatch = false
            )

            testNormaliseRegex(
                testName = "латинские заглавные буквы + цифры - всего 17 - без пробела - валиден",
                doc = vin.copy(value = "ABCDEFGHIJKLM1234".also { it.length shouldBe 17 }),
                isMatch = true
            )

            testNormaliseRegex(
                testName = "латинские заглавные буквы + цифры - всего 17 содержат пробел - не валиден",
                doc = vin.copy(value = "ABCDEFGHIJKLM 1234"),
                isMatch = false
            )

            testNormaliseRegex(
                testName = "шестнадцать цифр без пробела - не валиден",
                doc = vin.copy(value = "1234567890123456"),
                isMatch = false
            )

            testNormaliseRegex(
                testName = "шестнадцать цифр без пробела - не валиден",
                doc = vin.copy(value = "1234567890123456"),
                isMatch = false
            )

            testNormaliseRegex(
                testName = "восемнадцать цифр без пробела - не валиден",
                doc = vin.copy(value = "123456789012345678"),
                isMatch = false
            )

            testNormaliseRegex(
                testName = "шестнадцать заглавных латинских букв без пробела - не валиден",
                doc = vin.copy(value = "ABCDEFGHIJKLMNOP".also { it.length shouldBe 16 }),
                isMatch = false
            )

            testNormaliseRegex(
                testName = "восемнадцать заглавных латинских букв без пробела - не валиден",
                doc = vin.copy(value = "ABCDEFGHIJKLMNOPQR".also { it.length shouldBe 18 }),
                isMatch = false
            )

            testNormaliseRegex(
                testName = "семнадцать заглавных букв кириллица без пробела - не валиден",
                doc = vin.copy(value = "АБВГДЕЁЖЗИЙКЛМНОП".also { it.length shouldBe 17 }),
                isMatch = false
            )
        }

        context("Государственный регистрационный номер транспортного средства") {
            val grz = ExtractedDocument(docType = DocType.GRZ)

            testNormaliseRegex(
                testName = "С227НА69 - валиден",
                doc = grz.copy(value = "С227НА69"),
                isMatch = true
            )

            testNormaliseRegex(
                testName = "C227HA69 - написан латинскими буквами - не валиден",
                doc = grz.copy(value = "C227HA69"),
                isMatch = false
            )

            testNormaliseRegex(
                testName = "С227НА691 - регион из 3 цифр - валиден",
                doc = grz.copy(value = "С227НА691"),
                isMatch = true
            )

            testNormaliseRegex(
                testName = "С227НА6 - не заканчивается на две цифры - не валиден",
                doc = grz.copy(value = "С227НА6"),
                isMatch = false
            )

            testNormaliseRegex(
                testName = "С227Н69 - не содержит перед двумя цифрами две буквы - не валиден",
                doc = grz.copy(value = "С227Н69"),
                isMatch = false
            )

            testNormaliseRegex(
                testName = "НА69 - содержит только две цифры и две буквы - не валиден",
                doc = grz.copy(value = "НА69"),
                isMatch = false
            )
        }

        context("Свидетельство о регистрации транспортного средства") {
            val sts = ExtractedDocument(docType = DocType.STS)

            testNormaliseRegex(
                testName = "1234567890 - десять цифр - валиден",
                doc = sts.copy(value = "1234567890"),
                isMatch = true
            )

            testNormaliseRegex(
                testName = "12AA567890 - две цифры две буквы заглавные шесть цифр - валиден",
                doc = sts.copy(value = "12AA567890"),
                isMatch = true
            )

            testNormaliseRegex(
                testName = "12 AA 567890 - две цифры две буквы заглавные шесть цифр содержит пробел - не валиден",
                doc = sts.copy(value = "12 AA 567890"),
                isMatch = false
            )

            testNormaliseRegex(
                testName = "12aa567890 - две цифры две буквы строчные шесть цифр - не валиден",
                doc = sts.copy(value = "12aa567890"),
                isMatch = false
            )

            testNormaliseRegex(
                testName = "12AA56789 - две цифры две буквы строчные 5 цифр - не валиден",
                doc = sts.copy(value = "12AA56789"),
                isMatch = false
            )

            testNormaliseRegex(
                testName = "123456789 - девять цифр - не валиден",
                doc = sts.copy(value = "123456789"),
                isMatch = false
            )

            testNormaliseRegex(
                testName = "12345678901 - одиннадцать цифр - не валиден",
                doc = sts.copy(value = "12345678901"),
                isMatch = false
            )
        }

        context("ИНН физ.лица") {
            val innFl = ExtractedDocument(docType = DocType.INN_FL)

            testNormaliseRegex(
                testName = "двенадцать цифр без пробела - валиден",
                doc = innFl.copy(value = "123456789012"),
                isMatch = true
            )

            testNormaliseRegex(
                testName = "двенадцать цифр, содержит пробел - не валиден",
                doc = innFl.copy(value = "1234 56789012"),
                isMatch = false
            )

            testNormaliseRegex(
                testName = "одиннадцать цифр, не содержит пробел - не валиден",
                doc = innFl.copy(value = "12345678901"),
                isMatch = false
            )

            testNormaliseRegex(
                testName = "тринадцать цифр, не содержит пробел - не валиден",
                doc = innFl.copy(value = "1234567890123"),
                isMatch = false
            )
        }

        context("ИНН юр.лица") {
            val innUl = ExtractedDocument(docType = DocType.INN_UL)

            testNormaliseRegex(
                testName = "десять цифр без пробела - валиден",
                doc = innUl.copy(value = "0123456789"),
                isMatch = true
            )

            testNormaliseRegex(
                testName = "десять цифр, содержит пробел - не валиден",
                doc = innUl.copy(value = "0123 456789"),
                isMatch = false
            )

            testNormaliseRegex(
                testName = "девять цифр, не содержит пробел - не валиден",
                doc = innUl.copy(value = "123456789"),
                isMatch = false
            )

            testNormaliseRegex(
                testName = "одиннадцать цифр, не содержит пробел - не валиден",
                doc = innUl.copy(value = "01234567891"),
                isMatch = false
            )
        }

        context("ОГРН") {
            val ogrn = ExtractedDocument(docType = DocType.OGRN)

            testNormaliseRegex(
                testName = "тринадцать цифр без пробела - валиден",
                doc = ogrn.copy(value = "1234567890123"),
                isMatch = true
            )

            testNormaliseRegex(
                testName = "тринадцать цифр, содержит пробел - не валиден",
                doc = ogrn.copy(value = "1234 567890123"),
                isMatch = false
            )

            testNormaliseRegex(
                testName = "двенадцать цифр, не содержит пробел - не валиден",
                doc = ogrn.copy(value = "123456789012"),
                isMatch = false
            )

            testNormaliseRegex(
                testName = "четырнадцать цифр, не содержит пробел - не валиден",
                doc = ogrn.copy(value = "12345678901234"),
                isMatch = false
            )
        }

        context("ОГРНИП") {
            val ogrnip = ExtractedDocument(docType = DocType.OGRNIP)

            testNormaliseRegex(
                testName = "пятнадцать цифр без пробела - валиден",
                doc = ogrnip.copy(value = "123456789012345"),
                isMatch = true
            )

            testNormaliseRegex(
                testName = "пятнадцать цифр, содержит пробел - не валиден",
                doc = ogrnip.copy(value = "12 3456789012345"),
                isMatch = false
            )

            testNormaliseRegex(
                testName = "шестнадцать цифр, не содержит пробел - не валиден",
                doc = ogrnip.copy(value = "3456789012345"),
                isMatch = false
            )

            testNormaliseRegex(
                testName = "четырнадцать цифр, не содержит пробел - не валиден",
                doc = ogrnip.copy(value = "12345678901234"),
                isMatch = false
            )
        }
        context("СНИЛС") {
            val snils = ExtractedDocument(docType = DocType.SNILS)

            testNormaliseRegex(
                testName = "Всё через тире - валиден",
                doc = snils.copy(value = "123-456-789-00"),
                isMatch = true
            )

            testNormaliseRegex(
                testName = "Последние две цифры через пробел - не валиден",
                doc = snils.copy(value = "123-456-789 00"),
                isMatch = false
            )

            testNormaliseRegex(
                testName = "Все через тире - но содержит не одиннадцать цифр - не валиден",
                doc = snils.copy(value = "123-46-789-00"),
                isMatch = false
            )

            testNormaliseRegex(
                testName = "Без тире - не валиден",
                doc = snils.copy(value = "1234678900"),
                isMatch = false
            )
        }

    }
}
