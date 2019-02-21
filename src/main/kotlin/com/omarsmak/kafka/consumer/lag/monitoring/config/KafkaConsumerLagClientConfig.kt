@file:Suppress("MaximumLineLength", "MaxLineLength", "ImportOrdering")

package com.omarsmak.kafka.consumer.lag.monitoring.config

import java.util.Properties

open class KafkaConsumerLagClientConfig private constructor(
    private val configs: Map<String, Any>
) {
    companion object {
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

        /**
         * Create a [KafkaConsumerLagClientConfig] instance using a Map of configurations
         */
        fun create(prop: Map<String, Any>) = createAndValidateConfigs(prop)

        /**
         * Create a [KafkaConsumerLagClientConfig] instance using a [Properties] instance
         */
        @Suppress("UNCHECKED_CAST")
        fun create(prop: Properties) = createAndValidateConfigs(prop.toMap() as Map<String, Any>)

        private fun createAndValidateConfigs(prop: Map<String, Any>): KafkaConsumerLagClientConfig {
            // Check if we have our required BOOTSTRAP_SERVERS and CONSUMER_GROUPS
            if (prop.containsKey(BOOTSTRAP_SERVERS) && prop.containsKey(CONSUMER_GROUPS)){
                val configs = initializeDefaultConfig()
                // Iterate over supplied map and overwrite whatever we have
                prop.forEach { key, value ->
                    configs[key] = value
                }
                return KafkaConsumerLagClientConfig(configs.toMap())
            } else {
                throw KafkaConsumerLagClientConfigException("The required configurations 'BOOTSTRAP_SERVERS' and 'CONSUMER_GROUPS' don't exist in the supplied configurations.")
            }
        }

        private fun initializeDefaultConfig() = mutableMapOf(
                HTTP_PORT to DEFAULT_HTTP_PORT,
                CLIENT_TYPE to DEFAULT_CLIENT_TYPE,
                LAG_THRESHOLD to DEFAULT_LAG_THRESHOLD,
                POLL_INTERVAL to DEFAULT_POLL_INTERVAL
        )
    }

    /**
     * Get the value associated with a [key]
     */
    fun get(key: String): Any? {
        if (configs.containsKey(key)) return configs[key]
        else throw KafkaConsumerLagClientConfigException("Key '$key' does not exist in the configurations")
    }

    override fun toString(): String {
        return configs.toString()
    }
}