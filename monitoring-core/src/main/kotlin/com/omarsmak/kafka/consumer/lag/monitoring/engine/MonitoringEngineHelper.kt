package com.omarsmak.kafka.consumer.lag.monitoring.engine

import com.omarsmak.kafka.consumer.lag.monitoring.client.KafkaConsumerLagClient

object MonitoringEngineHelper {

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
