package codes.spectrum.conf2022

import io.kotest.core.spec.style.FunSpec
import io.kotest.core.test.TestCaseConfig

/**
 * Для тестов самой среды, по умолчанию их не запускаем
 */
internal abstract class InternalTest: FunSpec() {
    override fun defaultConfig(): TestCaseConfig {
        return super.defaultConfig().copy(enabled = SHOULD_RUN_INTERNAL_TESTS)
    }

    override fun defaultTestCaseConfig(): TestCaseConfig? {
        return (super.defaultTestCaseConfig() ?: TestCaseConfig()).copy(enabled = SHOULD_RUN_INTERNAL_TESTS)
    }


    companion object {
        val SHOULD_RUN_INTERNAL_TESTS = System.getenv("RUN_INTERNAL_TEST") == "true"
    }
}
