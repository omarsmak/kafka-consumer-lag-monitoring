package com.omarsmak.kafka.consumer.lag.monitoring.core

import com.omarsmak.kafka.consumer.lag.monitoring.client.KafkaConsumerLagClient

object ContextHelper {

    fun getTargetConsumerGroups(client: KafkaConsumerLagClient, configConsumerGroups: List<String>): Set<String> {
        // Get consumer groups from the kafka broker
        val consumerGroups = client.getConsumerGroupsList()

        // Fetch consumers based no a regex
        val matchedConsumersGroups = configConsumerGroups
                .filter { it.contains("*") }
                .map { x ->
                    consumerGroups.filter { it.startsWith(x.replace("*", "")) }
                }
                .flatten()

        return configConsumerGroups
                .filterNot { it.contains("*") }
                .union(matchedConsumersGroups)
    }
}
