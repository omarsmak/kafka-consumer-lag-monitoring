package com.omarsmak.kafka.consumer.lag.monitoring.cli

import com.omarsmak.kafka.consumer.lag.monitoring.client.KafkaConsumerLagClient
import java.io.File
import java.io.FileInputStream
import java.util.Properties

object Utils {
    fun readConfigFile(args: Array<String>, filePath: String) = Properties().apply {
        when (args.isEmpty()) {
            true -> Utils::class.java.classLoader.getResourceAsStream(filePath)
            false -> FileInputStream(File(args[0]))
        }.apply {
            load(this)
        }
    }

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
