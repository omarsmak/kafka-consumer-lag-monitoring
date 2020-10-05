package com.omarsmak.kafka.consumer.lag.monitoring.core

import com.omarsmak.kafka.consumer.lag.monitoring.client.KafkaConsumerLagClient
import com.omarsmak.kafka.consumer.lag.monitoring.client.data.Lag

interface Component {
    fun init(configs: Map<String, Any>)
    /**
     * Series of actions that can be executed upon starting the component
     */

    fun start()

    /**
     * Series of actions that can be executed upon stopping the component
     */
    fun stop()

    fun beforeProcess()

    fun process(consumerGroup : String, lag: List<Lag>, memberLag: Map<String, List<Lag>>)

    fun afterProcess()

    /**
     * Identifier for the component that can be used to parse the configs automatically by the context
     */
    fun identifier(): String
}