package com.omarsmak.kafka.consumer.lag.monitoring.support

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

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

    @Test
    fun testUpdateAndTrackConsumerGroups() {
       // first if both the same
        assertEquals(setOf("test"), Utils.updateAndTrackConsumerGroups(setOf("test"), setOf("test")).updatedConsumerGroups)

        // removed groups
        assertEquals(setOf("test"), Utils.updateAndTrackConsumerGroups(setOf("test-1", "test-2"), setOf("test"), false).updatedConsumerGroups)

        // added groups
        assertEquals(setOf("test-1", "test-2"), Utils.updateAndTrackConsumerGroups(setOf("test-1"), setOf("test-2", "test-1"), false).updatedConsumerGroups)

        // no groups
        assertTrue(Utils.updateAndTrackConsumerGroups(setOf("test-1"), emptySet(), false).updatedConsumerGroups.isEmpty())

        // new groups
        assertEquals(setOf("test"), Utils.updateAndTrackConsumerGroups(emptySet(), setOf("test")).updatedConsumerGroups)


        // with kept groups
        // removed groups
        assertEquals(setOf("test-1", "test-2","test"), Utils.updateAndTrackConsumerGroups(setOf("test-1", "test-2"), setOf("test")).updatedConsumerGroups)

        // added groups
        assertEquals(setOf("test-1", "test-2"), Utils.updateAndTrackConsumerGroups(setOf("test-1"), setOf("test-2", "test-1")).updatedConsumerGroups)

        // no groups
        assertEquals(setOf("test-1"), Utils.updateAndTrackConsumerGroups(setOf("test-1"), emptySet()).updatedConsumerGroups)
    }
}