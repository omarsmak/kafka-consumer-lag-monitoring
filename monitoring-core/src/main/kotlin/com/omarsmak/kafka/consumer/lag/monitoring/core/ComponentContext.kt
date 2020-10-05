package com.omarsmak.kafka.consumer.lag.monitoring.core

import com.omarsmak.kafka.consumer.lag.monitoring.client.KafkaConsumerLagClient
import com.omarsmak.kafka.consumer.lag.monitoring.client.KafkaConsumerLagClientFactory
import java.util.Timer
import kotlin.concurrent.scheduleAtFixedRate

class ComponentContext(configs: Map<String, Any>) {

    companion object {
        private const val CONFIG_LAG_CLIENT_PREFIX = "monitoring.lag"
        private const val CONFIG_KAFKA_PREFIX = "kafka"

        // default options
        private const val POLL_INTERVAL = "poll.interval"
        private const val DEFAULT_POLL_INTERVAL = 2000

        private const val CONSUMER_GROUPS = "consumer.groups"
    }

    val kafkaConfigs: Map<String, Any>
    val componentConfigs: Map<String, Any>

    private lateinit var component: Component
    private lateinit var kafkaConsumerLagClient: KafkaConsumerLagClient

    init {
        kafkaConfigs = prepareConfigs(configs, CONFIG_KAFKA_PREFIX)
        componentConfigs = initializeComponentDefaultConfigs().plus(prepareConfigs(configs, CONFIG_LAG_CLIENT_PREFIX))
    }

    fun registerComponent(component: Component) {
        this.component = component

        // initialize our component
        this.component.init(initializeSpecificComponentConfigs())
    }

    fun start() {
        // start our client
        kafkaConsumerLagClient = KafkaConsumerLagClientFactory.create(kafkaConfigs)

        // get our full target consumer groups
        val targetConsumerGroups = ContextHelper.getTargetConsumerGroups(kafkaConsumerLagClient, initializeConsumerGroups())

        // start our component
        component.start()

        // start our context and execute our component
        val monitoringPollInterval = componentConfigs[POLL_INTERVAL] as Long
        Timer().scheduleAtFixedRate(0, monitoringPollInterval) {
            // before we process the lag, call our component hook
            component.beforeProcess()

            // process our lag per consumer group
            targetConsumerGroups.forEach {
                val lag = kafkaConsumerLagClient.getConsumerLag(it)
                val memberLag = kafkaConsumerLagClient.getConsumerMemberLag(it)

                // process our lag per consumer
                component.process(it, lag, memberLag)
            }

            component.afterProcess()
        }
    }

    fun stop() {
        // stop our component
        component.stop()
    }

    private fun prepareConfigs(configs: Map<String, Any>, prefix: String) = configs
            .mapKeys { it.key.toLowerCase().replace("_", ".") }
            .filter {it.key.startsWith(prefix)}
            .mapKeys { it.key.removePrefix("$prefix.") }

    private fun initializeComponentDefaultConfigs() = mapOf(
            POLL_INTERVAL to DEFAULT_POLL_INTERVAL
    )

    private fun initializeConsumerGroups(): List<String> = (componentConfigs[CONSUMER_GROUPS] as String).split(",")

    private fun initializeSpecificComponentConfigs(): Map<String, Any> = componentConfigs.filter { it.key.startsWith(component.identifier()) }
            .mapKeys { it.key.removePrefix(component.identifier() + ".") }
}