package codes.spectrum.conf2022.output

/**
 *
 * */
enum class EntryConstraint() {
    /**
     * Содержит исключительно ожидаемый набор и ничего кроме
     * */
    EXACTLY,

    /**
     * Ожидаемый набор содержится в итоговый выборке
     * */
    CONTAINS;
}
