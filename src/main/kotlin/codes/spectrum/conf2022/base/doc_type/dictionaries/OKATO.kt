package codes.spectrum.conf2022.base.doc_type.dictionaries

object OKATO : BaseDictionary{
    private var _dict: Map<String, String>? = null

    override val dict: Map<String, String>
        get() {
            return if (_dict != null) _dict!!
            else {
                _dict = REGIONS.getDictionary()
                _dict!!
            }
        }

    override val codesAndValue = """
        01	Алтайский край
        03	Краснодарский край
        04	Красноярский край
        05	Приморский край
        07	Ставропольский край
        08	Хабаровский край
        10	Амурская область
        11	Архангельская область
        12	Астраханская область
        14	Белгородская область
        15	Брянская область
        17	Владимирская область
        18	Волгоградская область
        19	Вологодская область
        20	Воронежская область
        22	Нижегородская область
        24	Ивановская область
        25	Иркутская область
        26	Республика Ингушетия
        27	Калининградская область
        28	Тверская область
        29	Калужская область
        30	Камчатская область
        32	Кемеровская область
        33	Кировская область
        34	Костромская область
        36	Самарская область
        37	Курганская область
        38	Курская область
        40	Город Санкт-Петербург
        41	Ленинградская область
        42	Липецкая область
        44	Магаданская область
        45	Город Москва
        46	Московская область
        47	Мурманская область
        49	Новгородская область
        50	Новосибирская область
        52	Омская область
        53	Оренбургская область
        54	Орловская область
        56	Пензенская область
        57	Пермский край
        58	Псковская область
        60	Ростовская область
        61	Рязанская область
        63	Саратовская область
        64	Сахалинская область
        65	Свердловская область
        66	Смоленская область
        68	Тамбовская область
        69	Томская область
        70	Тульская область
        71	Тюменская область
        73	Ульяновская область
        75	Челябинская область
        76	Забайкальский край
        77	Чукотский автономный округ
        78	Ярославская область
        79	Республика Адыгея (Адыгея)
        80	Республика Башкортостан
        81	Республика Бурятия
        82	Республика Дагестан
        83	Кабардино-Балкарская Р-ка
        84	Республика Алтай
        85	Республика Калмыкия
        86	Республика Карелия
        87	Республика Коми
        88	Республика Марий Эл
        89	Республика Мордовия
        90	Р-ка Северная Осетия-Алания
        91	Карачаево-Черкесская Р-ка
        92	Республика Татарстан
        93	Республика Тыва
        94	Удмуртская Республика
        95	Республика Хакасия
        96	Чеченская Республика
        97	Чувашская Республика
        98	Республика Саха (Якутия)
        99	Еврейская автономная область
        """
}

