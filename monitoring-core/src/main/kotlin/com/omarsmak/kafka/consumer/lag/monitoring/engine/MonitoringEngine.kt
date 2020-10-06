package com.omarsmak.kafka.consumer.lag.monitoring.engine

import com.omarsmak.kafka.consumer.lag.monitoring.client.KafkaConsumerLagClient
import com.omarsmak.kafka.consumer.lag.monitoring.client.KafkaConsumerLagClientFactory
import com.omarsmak.kafka.consumer.lag.monitoring.component.MonitoringComponent
import mu.KotlinLogging
import java.util.Timer
import kotlin.concurrent.scheduleAtFixedRate

class MonitoringEngine private constructor(monitoringComponent: MonitoringComponent, configs: Map<String, Any>) {

    companion object {
        private val logger = KotlinLogging.logger {}

        const val CONFIG_LAG_CLIENT_PREFIX = "monitoring.lag"
        const val CONFIG_KAFKA_PREFIX = "kafka"

        // default options
        private const val POLL_INTERVAL_POSTFIX = "poll.interval"
        private const val DEFAULT_POLL_INTERVAL = 2000

        private const val CONSUMER_GROUPS_POSTFIX = "consumer.groups"

        // exposed options that we expect in the engine
        const val POLL_INTERVAL = "$CONFIG_LAG_CLIENT_PREFIX.$POLL_INTERVAL_POSTFIX"
        const val CONSUMER_GROUPS = "$CONFIG_LAG_CLIENT_PREFIX.$CONSUMER_GROUPS_POSTFIX"

        fun createWithComponentAndConfigs(monitoringComponent: MonitoringComponent, configs: Map<String, Any>): MonitoringEngine =
                MonitoringEngine(monitoringComponent, configs)
    }

    val kafkaConfigs: Map<String, Any>
    val componentConfigs: Map<String, Any>

    private lateinit var monitoringComponent: MonitoringComponent
    private lateinit var kafkaConsumerLagClient: KafkaConsumerLagClient

    init {
        kafkaConfigs = prepareConfigs(configs, CONFIG_KAFKA_PREFIX)
        componentConfigs = initializeComponentDefaultConfigs().plus(prepareConfigs(configs, CONFIG_LAG_CLIENT_PREFIX))

        registerComponent(monitoringComponent)
    }

    fun start() {
        // start our client
        kafkaConsumerLagClient = KafkaConsumerLagClientFactory.create(kafkaConfigs)

        // start our component
        monitoringComponent.start()

        // start our context and execute our component
        val monitoringPollInterval = componentConfigs[POLL_INTERVAL_POSTFIX] as Int

        logger.info("Updating metrics every $monitoringPollInterval...")

        Timer().scheduleAtFixedRate(0, monitoringPollInterval.toLong()) {
            // get our full target consumer groups, however we do have to check here to make sure we catch any new consumer group
            val targetConsumerGroups = MonitoringEngineHelper.getTargetConsumerGroups(kafkaConsumerLagClient, initializeConsumerGroups())

            // before we process the lag, call our component hook
            monitoringComponent.beforeProcess()

            // process our lag per consumer group
            targetConsumerGroups.forEach {
                val lag = kafkaConsumerLagClient.getConsumerLag(it)
                val memberLag = kafkaConsumerLagClient.getConsumerMemberLag(it)

                // process our lag per consumer
                monitoringComponent.process(it, lag, memberLag)
            }

            monitoringComponent.afterProcess()
        }
    }

    fun stop() {
        // stop our client
        kafkaConsumerLagClient.close()

        // stop our component
        monitoringComponent.stop()
    }

    private fun registerComponent(monitoringComponent: MonitoringComponent) {
        this.monitoringComponent = monitoringComponent

        // initialize our component
        this.monitoringComponent.configure(initializeSpecificComponentConfigs())
    }

    private fun prepareConfigs(configs: Map<String, Any>, prefix: String) = configs
            .mapKeys { it.key.toLowerCase().replace("_", ".") }
            .filter {it.key.startsWith(prefix)}
            .mapKeys { it.key.removePrefix("$prefix.") }

    private fun initializeComponentDefaultConfigs() = mapOf(
            POLL_INTERVAL_POSTFIX to DEFAULT_POLL_INTERVAL
    )

    private fun initializeConsumerGroups(): List<String> = (componentConfigs[CONSUMER_GROUPS_POSTFIX] as String).split(",")

    private fun initializeSpecificComponentConfigs(): Map<String, Any> =
            componentConfigs.filter { it.key.startsWith(monitoringComponent.identifier()) }
            .mapKeys { it.key.removePrefix(monitoringComponent.identifier() + ".") }
}