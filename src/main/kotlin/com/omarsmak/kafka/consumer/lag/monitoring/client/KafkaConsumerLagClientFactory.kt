package com.omarsmak.kafka.consumer.lag.monitoring.client

import com.omarsmak.kafka.consumer.lag.monitoring.client.exceptions.KafkaConsumerLagClientException
import com.omarsmak.kafka.consumer.lag.monitoring.client.impl.KafkaConsumerLagJavaClient
import com.omarsmak.kafka.consumer.lag.monitoring.client.impl.KafkaConsumerLagScalaClient
import java.util.*

/**
 * Main KafkaOffset factory, this should be the entry
 *
 * @author oalsafi
 */
object KafkaConsumerLagClientFactory {
    @JvmStatic
    fun getClient(client: String, prop: Properties): KafkaConsumerLagClient =
            when (client.trim().toLowerCase()) {
                "scala" -> KafkaConsumerLagScalaClient.create(prop)
                "java" -> KafkaConsumerLagJavaClient.create(prop)
                else -> throw KafkaConsumerLagClientException("$client is not a known client type!")
            }
}
