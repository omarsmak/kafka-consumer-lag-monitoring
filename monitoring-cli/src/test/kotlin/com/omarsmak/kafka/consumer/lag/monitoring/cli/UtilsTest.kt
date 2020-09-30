package com.omarsmak.kafka.consumer.lag.monitoring.cli

import org.junit.Test
import org.junit.jupiter.api.Assertions.*

internal class UtilsTest {

    @Test
    fun `test if loads properties` () {
        val resourcesPath = javaClass.classLoader.getResource("config/consumer-monitoring.properties")!!.path

        val props = Utils.loadPropertiesFile(resourcesPath)

        assertEquals("test:9092", props.getProperty("bootstrap.servers"))
    }
}