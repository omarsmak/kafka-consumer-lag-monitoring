package com.omarsmak.kafka.consumer.lag.monitoring.engine

import com.omarsmak.kafka.consumer.lag.monitoring.client.KafkaConsumerLagClient
import com.omarsmak.kafka.consumer.lag.monitoring.client.KafkaConsumerLagClientFactory
import com.omarsmak.kafka.consumer.lag.monitoring.component.MonitoringComponent
import com.omarsmak.kafka.consumer.lag.monitoring.data.ConsumerGroupLag
import com.omarsmak.kafka.consumer.lag.monitoring.support.Utils
import com.omarsmak.kafka.consumer.lag.monitoring.support.castToLong
import mu.KotlinLogging
import java.lang.IllegalArgumentException
import java.lang.NumberFormatException
import java.util.Timer
import kotlin.concurrent.scheduleAtFixedRate

class MonitoringEngine private constructor(monitoringComponent: MonitoringComponent, configs: Map<String, Any?>) {

    companion object {
        private val logger = KotlinLogging.logger {}

        const val CONFIG_LAG_CLIENT_PREFIX = "monitoring.lag"
        const val CONFIG_KAFKA_PREFIX = "kafka"

        // default options
        const val POLL_INTERVAL = "poll.interval"
        const val DEFAULT_POLL_INTERVAL = 2000

        const val CONSUMER_GROUPS = "consumer.groups"

        fun createWithComponentAndConfigs(monitoringComponent: MonitoringComponent, configs: Map<String, Any?>): MonitoringEngine =
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
        // validate our configs
        validateComponentConfigs(CONSUMER_GROUPS, String, true)

        // start our client
        kafkaConsumerLagClient = KafkaConsumerLagClientFactory.create(kafkaConfigs)

        // start our component
        monitoringComponent.start()

        // start our context and execute our component
        val monitoringPollInterval = getConfigAsLong(POLL_INTERVAL)

        logger.info("Updating metrics every $monitoringPollInterval...")

        Timer().scheduleAtFixedRate(0, monitoringPollInterval) {
            // get our full target consumer groups, however we do have to check here to make sure we catch any new consumer group
            val targetConsumerGroups = Utils.getTargetConsumerGroups(kafkaConsumerLagClient, initializeConsumerGroups())

            // before we process the lag, call our component hook
            monitoringComponent.beforeProcess()

            // process our lag per consumer group
            targetConsumerGroups.forEach {
                val lag = kafkaConsumerLagClient.getConsumerLag(it)
                val memberLag = kafkaConsumerLagClient.getConsumerMemberLag(it)

                // process our lag per consumer
                monitoringComponent.process(it, ConsumerGroupLag(it, lag, memberLag))
            }

            // after we are done, we call our component hook
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

    private fun prepareConfigs(configs: Map<String, Any?>, prefix: String): Map<String, Any> = configs
            .mapKeys { it.key.toLowerCase().replace("_", ".") }
            .filter {it.key.startsWith(prefix) && it.value != null}
            .mapKeys { it.key.removePrefix("$prefix.") }
            .mapValues { it.value as Any }

    private fun initializeComponentDefaultConfigs() = mapOf(
            POLL_INTERVAL to DEFAULT_POLL_INTERVAL
    )

    private fun <T> validateComponentConfigs(key:String, type: T, required: Boolean) {
        val value:Any? = componentConfigs[key]

        // check if exists
        if (required && value == null) throw IllegalArgumentException("Missing required configuration '$key' which has no default value.")

        // check type
        if (type is String) {
            val valueAsString: String = value as String
            if (required && valueAsString.isEmpty()) throw IllegalArgumentException("Missing required configuration '$key' which has no default value.")
        }
    }

    private fun getConfigAsLong(key: String) = try {
        componentConfigs.getValue(key).castToLong()
    } catch (e: NumberFormatException) {
        throw IllegalArgumentException("The value '" + componentConfigs[key] + "' of key '$key' cannot be converted to long")
    }

    private fun initializeConsumerGroups(): List<String> {
        val consumerGroups = componentConfigs[CONSUMER_GROUPS] as String?

        if (consumerGroups.isNullOrEmpty()) {
            throw IllegalArgumentException("Missing required configuration '$CONSUMER_GROUPS' which has no default value.")
        }

        return  consumerGroups.split(",")
    }


    private fun initializeSpecificComponentConfigs(): Map<String, Any> =
            componentConfigs.filter { it.key.startsWith(monitoringComponent.identifier()) }
            .mapKeys { it.key.removePrefix(monitoringComponent.identifier() + ".") }
}