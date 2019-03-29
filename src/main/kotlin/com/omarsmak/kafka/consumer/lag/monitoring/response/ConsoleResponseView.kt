@file:Suppress("ParameterListWrapping")

package com.omarsmak.kafka.consumer.lag.monitoring.response

import com.github.ajalt.mordant.TermColors
import com.omarsmak.kafka.consumer.lag.monitoring.client.KafkaConsumerLagClient
import com.omarsmak.kafka.consumer.lag.monitoring.client.data.Lag
import com.omarsmak.kafka.consumer.lag.monitoring.client.exceptions.KafkaConsumerLagClientException
import com.omarsmak.kafka.consumer.lag.monitoring.config.KafkaConsumerLagClientConfig
import java.util.*
import kotlin.concurrent.scheduleAtFixedRate

/**
 * This class prints the lag outputs to the console based on the configuration passed
 */
class ConsoleResponseView : ResponseView {
    private lateinit var kafkaConsumerLagClient: KafkaConsumerLagClient
    private lateinit var kafkaConsumerLagClientConfig: KafkaConsumerLagClientConfig
    private val termColors = TermColors()

    override fun configure(kafkaConsumerLagClient: KafkaConsumerLagClient, config: KafkaConsumerLagClientConfig) {
        this.kafkaConsumerLagClient = kafkaConsumerLagClient
        this.kafkaConsumerLagClientConfig = config
    }

    override fun execute() {
        val targetConsumerGroups: Set<String> = kafkaConsumerLagClientConfig[KafkaConsumerLagClientConfig.CONSUMER_GROUPS]
        val monitoringPollInterval: Long = kafkaConsumerLagClientConfig[KafkaConsumerLagClientConfig.POLL_INTERVAL]
        val monitoringLagThreshold: Int = kafkaConsumerLagClientConfig[KafkaConsumerLagClientConfig.LAG_THRESHOLD]

        show(targetConsumerGroups, monitoringPollInterval, monitoringLagThreshold)
    }

    override fun identifier() = "console"

    /**
     * Print [targetConsumerGroups] outputs to the console in this format:
     * `Topic name: @topicName`
     * `Total topic offsets: @topicOffsets`
     * `Total consumer offsets: @totalConsumerOffsets`
     * `Total lag: @totalLag`
     */
    private fun show(targetConsumerGroups: Set<String>, monitoringPollInterval: Long, monitoringLagThreshold: Int) {
        Timer().scheduleAtFixedRate(0, monitoringPollInterval) {
            print("\u001b[H\u001b[2J")

            targetConsumerGroups.forEach { consumer ->
                try {
                    val metrics = kafkaConsumerLagClient.getConsumerLag(consumer)
                    println("Consumer group: $consumer")
                    println("==============================================================================")
                    println()
                    metrics.forEach {
                        printLagPerTopic(it, monitoringLagThreshold, termColors)
                    }
                } catch (e: KafkaConsumerLagClientException) {
                    println(termColors.yellow("Warning:${e.message}"))
                }
            }
            println()
        }
    }

    private fun printLagPerTopic(metrics: Lag, monitoringLagThreshold: Int, termColors: TermColors) {
        println("Topic name: ${metrics.topicName}")
        println("Total topic offsets: ${metrics.latestTopicOffsets.values.sum()}")
        println("Total consumer offsets: ${metrics.latestConsumerOffsets.values.sum()}")

        when (metrics.totalLag) {
            in 0..monitoringLagThreshold ->
                println("Total lag: ${termColors.green(metrics.totalLag.toString())}")
            in monitoringLagThreshold..monitoringLagThreshold * 2 ->
                println("Total lag: ${termColors.yellow(metrics.totalLag.toString())}")
            else ->
                println("Total lag: ${termColors.red(metrics.totalLag.toString())}")
        }
        println()
    }
}
