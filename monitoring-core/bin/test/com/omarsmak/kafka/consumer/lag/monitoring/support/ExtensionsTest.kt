package com.omarsmak.kafka.consumer.lag.monitoring.support

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals


class ExtensionsTest {

    @Test
    fun toIntTest() {
        val test: Any = "12345"
        val test2: Any = 12345L

        assertEquals(12345, test.castToInt())
        assertEquals(12345, test2.castToInt())
    }

    @Test
    fun toLongTest() {
        val test: Any = "12345"
        val test2: Any = 12345

        assertEquals(12345L, test.castToLong())
        assertEquals(12345L, test2.castToLong())
    }
}