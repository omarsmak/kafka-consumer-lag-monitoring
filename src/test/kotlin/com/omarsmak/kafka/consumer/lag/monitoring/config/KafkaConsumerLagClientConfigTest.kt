package com.omarsmak.kafka.consumer.lag.monitoring.config

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class KafkaConsumerLagClientConfigTest {
    @Test
    fun `test a functions`(){
        val config = KafkaConsumerLagClientConfig.create(mapOf(
                KafkaConsumerLagClientConfig.BOOTSTRAP_SERVERS to "dd",
                KafkaConsumerLagClientConfig.CONSUMER_GROUPS to setOf("dd", "e")
        ))

        println(config.toString())
    }
}