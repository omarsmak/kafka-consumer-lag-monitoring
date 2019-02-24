package com.omarsmak.kafka.consumer.lag.monitoring.client

import com.omarsmak.kafka.consumer.lag.monitoring.client.impl.KafkaConsumerLagJavaClient
import com.omarsmak.kafka.consumer.lag.monitoring.config.KafkaConsumerLagClientConfig
import java.util.Properties
import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.Serdes

/**
 * Main KafkaOffset factory, this should be the entry
 *
 * @author oalsafi
 */
object KafkaConsumerLagClientFactory {
    @JvmStatic
    fun create(prop: Properties): KafkaConsumerLagClient = createJavaClient(prop)

    @JvmStatic
    fun create(config: KafkaConsumerLagClientConfig): KafkaConsumerLagClient = createJavaClient(config.toProperties())

    @JvmStatic
    fun create(map: Map<String, Any>): KafkaConsumerLagClient = createJavaClient(Properties().apply {
        putAll(map)
    })

    private fun createJavaClient(prop: Properties): KafkaConsumerLagJavaClient {
        val adminClient = AdminClient.create(prop)
        val consumerClient = KafkaConsumer(prop, Serdes.String().deserializer(), Serdes.String().deserializer())

        return KafkaConsumerLagJavaClient(adminClient, consumerClient)
    }
}
