package com.omarsmak.kafka.consumer.lag.monitoring.component.console

import com.github.ajalt.mordant.TermColors
import com.omarsmak.kafka.consumer.lag.monitoring.client.data.Lag
import com.omarsmak.kafka.consumer.lag.monitoring.component.MonitoringComponent
import com.omarsmak.kafka.consumer.lag.monitoring.data.ConsumerGroupLag
import com.omarsmak.kafka.consumer.lag.monitoring.support.castToInt

class ConsoleMonitoringComponent : MonitoringComponent {

    companion object {
        const val LAG_THRESHOLD = "lag.threshold"
        const val DEFAULT_LAG_THRESHOLD = 500
    }

    private var monitoringLagThreshold = DEFAULT_LAG_THRESHOLD
    private val termColors = TermColors()

    override fun configure(configs: Map<String, Any>) {
        monitoringLagThreshold = configs.getOrDefault(LAG_THRESHOLD, DEFAULT_LAG_THRESHOLD).castToInt()
    }

    override fun start() {
    }

    override fun stop() {
    }

    override fun beforeProcess() {
        print("\u001b[H\u001b[2J")
    }

    override fun process(consumerGroup: String, consumerGroupLag: ConsumerGroupLag) {
        println("Consumer group: $consumerGroup")
        println("==============================================================================")
        println()
        consumerGroupLag.lag.forEach {
            printLagPerTopic(it, monitoringLagThreshold, termColors)
        }
    }

    override fun afterProcess() {
        println()
    }

    override fun identifier(): String = "console"

    override fun onError(t: Throwable) {
        println(termColors.yellow("Warning:${t.message}"))
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