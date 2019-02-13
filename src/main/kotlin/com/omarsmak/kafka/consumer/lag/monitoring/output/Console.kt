@file:Suppress("ParameterListWrapping")

package com.omarsmak.kafka.consumer.lag.monitoring.output

import com.github.ajalt.mordant.TermColors
import com.omarsmak.kafka.consumer.lag.monitoring.client.KafkaConsumerLagClient
import com.omarsmak.kafka.consumer.lag.monitoring.client.data.Lag
import com.omarsmak.kafka.consumer.lag.monitoring.client.exceptions.KafkaConsumerLagClientException
import java.util.*
import kotlin.concurrent.scheduleAtFixedRate

/**
 * This class prints the lag output to the console based on the configuration passed
 */
class Console(
        private val client: KafkaConsumerLagClient,
        private val monitoringTimeout: Long,
        private val monitoringLagThreshold: Long
) {

    private val termColors = TermColors()

    /**
     * Print [targetConsumerGroups] output to the console in this format:
     * `Topic name: @topicName`
     * `Total topic offsets: @topicOffsets`
     * `Total consumer offsets: @totalConsumerOffsets`
     * `Total lag: @totalLag`
     */
    fun show(targetConsumerGroups: Set<String>) {
        Timer().scheduleAtFixedRate(0, monitoringTimeout) {
            print("\u001b[H\u001b[2J")

            targetConsumerGroups.forEach { consumer ->
                try {
                    val metrics = client.getConsumerLag(consumer)
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

    private fun printLagPerTopic(metrics: Lag, monitoringLagThreshold: Long, termColors: TermColors) {
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
