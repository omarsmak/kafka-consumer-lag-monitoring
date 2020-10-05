package com.omarsmak.kafka.consumer.lag.monitoring.core

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class ComponentContextTest {

    @Test
    fun testConfigs() {
        val configs = mapOf(
                "kafka.bootstrap.server" to "localhost:9090" ,
                "kafka.poll.interval" to "900",
                "KAFKA_FETCH_RATE" to "100",
                "monitoring.lag.consumer.groups" to "test1,test2",
                "monitoring.lag.datadog.poll.interval" to "300"
        )

        val context = ComponentContext(configs)

        // assert only kafka configs
        assertEquals(3, context.kafkaConfigs.size)
        assertEquals("localhost:9090", context.kafkaConfigs["bootstrap.server"])
        assertEquals("900", context.kafkaConfigs["poll.interval"])
        assertEquals("100", context.kafkaConfigs["fetch.rate"])

        // assert only lag configs
        assertEquals(3, context.componentConfigs.size)
        assertEquals("test1,test2", context.componentConfigs["consumer.groups"])
        assertEquals("300", context.componentConfigs["datadog.poll.interval"])
        assertEquals(2000, context.componentConfigs["poll.interval"])
    }
}