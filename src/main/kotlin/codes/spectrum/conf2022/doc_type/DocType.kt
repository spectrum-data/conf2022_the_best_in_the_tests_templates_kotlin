package codes.spectrum.conf2022.doc_type

/**
 *
 * */
enum class DocType {
    UNDEFINED,

    /**
     * Паспорт РФ
     * */
    PASSPORT_RF,

    /**
     * Водительское удостоверение
     * */
    DRIVER_LICENSE,

    /**
     * Идентификационный номер транспортного средства
     * */
    VIN,

    /**
     * Государственный регистрационный номер транспортного средства
     * */
    GRZ,

    /**
     * Свидетельство о регистрации транспортного средства
     * */
    STS,

    /**
     * ИНН Юр. лица
     * */
    INN_FL,

    /**
     * ИНН Физ. лица
     * */
    INN_UL,

    /**
     * ОГРН
     * */
    OGRN,

    /**
     * ОГРНИП
     * */
    OGRNIP,

    /**
     * СНИЛС
     * */
    SNILS,
}

fun getNormaliseValueRegex(docType: DocType): Regex {
    return when (docType) {
        DocType.PASSPORT_RF -> "\\d{10}"
        DocType.DRIVER_LICENSE -> "\\d{10}"
        DocType.VIN -> "[A-Z0-9]{17}"
        DocType.GRZ -> "[АВЕКМНОРСТУХ]\\d{3}[АВЕКМНОРСТУХ]{2}\\d{2,3}"
        DocType.STS -> "\\d{2}[А-ЯA-Z0-9]{2}\\d{6}"
        DocType.INN_FL -> "\\d{12}"
        DocType.INN_UL -> "\\d{10}"
        DocType.OGRN -> "\\d{13}"
        DocType.OGRNIP -> "\\d{15}"
        DocType.SNILS -> "\\d{3}-\\d{3}-\\d{3}-\\d{2}"
        DocType.UNDEFINED -> error("Попытка получить регулярное выражение для нормализации неопознанного документа - $docType")
    }.toRegex()
}