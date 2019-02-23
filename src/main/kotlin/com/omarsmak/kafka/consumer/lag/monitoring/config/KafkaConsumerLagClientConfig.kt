@file:Suppress("MaximumLineLength", "MaxLineLength", "ImportOrdering")

package com.omarsmak.kafka.consumer.lag.monitoring.config

import com.uchuhimo.konf.Config
import com.uchuhimo.konf.ConfigException
import com.uchuhimo.konf.ConfigSpec
import java.util.Properties

open class KafkaConsumerLagClientConfig private constructor(
    private val configs: Config
) {
    companion object : ConfigSpec(prefix = "") {
        const val HTTP_PORT = "http.port"
        private const val DEFAULT_HTTP_PORT = 9000

        const val BOOTSTRAP_SERVERS = "bootstrap.servers"

        const val POLL_INTERVAL = "poll.interval"
        private const val DEFAULT_POLL_INTERVAL = 200

        const val CONSUMER_GROUPS = "consumer.groups"

        const val CLIENT_TYPE = "client.type"
        private const val DEFAULT_CLIENT_TYPE = "java"

        const val LAG_THRESHOLD = "lag.threshold"
        private const val DEFAULT_LAG_THRESHOLD = 500

        // Add typesafe configuration definitions
        private val httpPort by optional(name = HTTP_PORT, default = DEFAULT_HTTP_PORT)
        private val bootstrapServers by required<String>(name = BOOTSTRAP_SERVERS)
        private val pollInterval by optional(name = POLL_INTERVAL, default = DEFAULT_POLL_INTERVAL)
        private val consumerGroups by required<Set<String>>(name = CONSUMER_GROUPS)
        private val clientType by optional(name = CLIENT_TYPE, default = DEFAULT_CLIENT_TYPE)
        private val lagTreshold by optional(name = LAG_THRESHOLD, default = DEFAULT_LAG_THRESHOLD)

        /**
         * Create a [KafkaConsumerLagClientConfig] instance using a Map of configurations
         */
        @JvmStatic
        fun create(prop: Map<String, Any>) = createAndValidateConfigs(prop)

        /**
         * Create a [KafkaConsumerLagClientConfig] instance using a [Properties] instance
         */
        @JvmStatic
        @Suppress("UNCHECKED_CAST")
        fun create(prop: Properties) = createAndValidateConfigs(prop.toMap() as Map<String, Any>)

        private fun createAndValidateConfigs(prop: Map<String, Any>): KafkaConsumerLagClientConfig {
            try {
                val config = Config {
                    addSpec(this@Companion)
                }.from.map.kv(prop)
                return KafkaConsumerLagClientConfig(config)
            } catch (ex: ConfigException){
                throw KafkaConsumerLagClientConfigException(ex)
            }
        }
    }

    /**
     * Get the value associated with a [key]
     */
    operator fun get(key: String): Any? {
        try {
            return configs[key]
        } catch (ex: ConfigException) {
            throw KafkaConsumerLagClientConfigException(ex)
        }
    }

    override fun toString(): String {
        return configs.toString()
    }
}