package com.omarsmak.kafka.consumer.lag.monitoring.cli

import com.omarsmak.kafka.consumer.lag.monitoring.client.KafkaConsumerLagClient
import com.omarsmak.kafka.consumer.lag.monitoring.config.KafkaConsumerLagClientConfig
import com.omarsmak.kafka.consumer.lag.monitoring.response.ResponseView
import mu.KotlinLogging
import org.reflections.Reflections
import java.io.File
import java.io.FileInputStream
import java.util.*

object Utils {
    private val logger = KotlinLogging.logger {}

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

    fun loadResponseViewPlugins(kafkaConsumerLagClient: KafkaConsumerLagClient, kafkaConsumerLagClientConfig: KafkaConsumerLagClientConfig): Set<ResponseView> {
        // The reason for me to use reflection here is that I am planning to add dynamic loading for plugins, meaning that we can
        // add the ability to add plugin through CLASSPATH or perhaps by creating a dedicated plugin folder
        val reflections = Reflections(ResponseView::class.java.`package`.name)
        val clazzes = reflections.getSubTypesOf(ResponseView::class.java)

       logger.info("loaded the following response view plugins: ${clazzes.joinToString(",")}")

        return clazzes.map {
            // Initiate objects
            it.getConstructor().newInstance().apply { configure(kafkaConsumerLagClient, kafkaConsumerLagClientConfig) }
        }.toSet()
    }
}
