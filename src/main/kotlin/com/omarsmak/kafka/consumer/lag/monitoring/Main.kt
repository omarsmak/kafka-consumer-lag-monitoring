package com.omarsmak.kafka.consumer.lag.monitoring

import com.omarsmak.kafka.consumer.lag.monitoring.client.KafkaConsumerLagClientFactory
import com.omarsmak.kafka.consumer.lag.monitoring.output.Console
import com.omarsmak.kafka.consumer.lag.monitoring.output.Prometheus
import java.io.IOException
import mu.KotlinLogging

/**
 * Main entry for the program
 *
 * @author oalsafi
 * @since 2018-07-16
 */

private val logger = KotlinLogging.logger {}

fun main(arg: Array<String>) {

    try {
        // Load config
        val config = Utils.readConfigFile(arg, "config/consumer-monitoring.properties")
        val configConsumerGroups = config.getProperty("monitoring.consumers").toString().split(",")
        val monitoringTimeout = config.getProperty("monitoring.timeout.ms").toLong()
        val monitoringLagThreshold = config.getProperty("monitoring.consumer.lag.threshold").toLong()
        val monitoringOutputMode = config.getProperty("monitoring.output.mode").toString()
        val monitoringOutputHttpPort = config.getProperty("monitoring.output.http.port").toInt()
        val monitoringClient = config.getProperty("monitoring.client.type").toString()

        // Create client
        val kafkaOffsetClient = KafkaConsumerLagClientFactory.getClient(monitoringClient, config)

        // Scrap the consumer groups
        val targetConsumerGroups = Utils.getTargetConsumerGroups(kafkaOffsetClient, configConsumerGroups)

        when (monitoringOutputMode) {
            "console" ->
                Console(kafkaOffsetClient, monitoringTimeout, monitoringLagThreshold)
                        .show(targetConsumerGroups)
            "prometheus" ->
                Prometheus(kafkaOffsetClient, monitoringTimeout)
                        .initialize(targetConsumerGroups, monitoringOutputHttpPort)
            else -> logger.error("Output mode `$monitoringOutputMode` is not valid")
        }
    } catch (e: IOException) {
        logger.error { e.message }
    }
}
