package com.omarsmak.kafka.consumer.lag.monitoring.support

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class UtilsTest {

    @Test
    fun testGetConfigsWithPrefixCaseSensitive() {
        val configs = mapOf(
                "PREFIX_TEST_test_configTest_1" to "test-config-1",
                "prefix.test.test.configTest.2" to "test-config-2"
        )

        val parsedConfigs = Utils.getConfigsWithPrefixCaseSensitive(configs, "prefix.test")

        assertEquals("test-config-1", parsedConfigs["test.configTest.1"])
        assertEquals("test-config-2", parsedConfigs["test.configTest.2"])
    }

    @Test
    fun testGetConfigsWithPrefixCaseInSensitive() {
        val configs = mapOf(
                "PREFIX_TEST_test_configTest_1" to "test-config-1",
                "prefix.test.test.configTest.2" to "test-config-2"
        )

        val parsedConfigs = Utils.getConfigsWithPrefixCaseInSensitive(configs, "prefix.test")

        assertEquals("test-config-1", parsedConfigs["test.configtest.1"])
        assertEquals("test-config-2", parsedConfigs["test.configtest.2"])
    }
}