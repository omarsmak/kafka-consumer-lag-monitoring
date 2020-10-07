package com.omarsmak.kafka.consumer.lag.monitoring.support

import com.omarsmak.kafka.consumer.lag.monitoring.client.KafkaConsumerLagClient
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.util.Properties

object Utils {

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

    fun loadPropertiesFile(filePath: String): Properties {
        val inputStream: FileInputStream? = File(filePath).inputStream()
        val prop = Properties()

        if (inputStream == null) {
            throw FileNotFoundException("File '$filePath' not found!")
        }

        prop.load(inputStream)

        return prop
    }

    fun loadPropertiesFileAsMap(filePath: String): Map<String, Any?> {
        val props = loadPropertiesFile(filePath)

        return props.toMap()
                .mapKeys { it.key as String }
    }

    fun getConfigsFromPropertiesFileOrFromEnv(arg: Array<String>): Map<String, Any?> {
        // first we try to load the from properties file using provided args
        // if we fail to find something, we use the env variables as fall back
        return if (arg.isNotEmpty()) loadPropertiesFileAsMap(arg[0]) else System.getenv()
    }
}
