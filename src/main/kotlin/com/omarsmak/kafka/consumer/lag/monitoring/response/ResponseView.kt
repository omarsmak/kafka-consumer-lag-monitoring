package com.omarsmak.kafka.consumer.lag.monitoring.response

import com.omarsmak.kafka.consumer.lag.monitoring.client.KafkaConsumerLagClient
import com.omarsmak.kafka.consumer.lag.monitoring.config.KafkaConsumerLagClientConfig

/**
 * Interface for [ResponseView] public API
 *
 * @author oalsafi
 */

interface ResponseView {
    /**
     * Configure a response view task with instance of [KafkaConsumerLagClientConfig],
     * this will be called upon initializing the client
     */
    fun configure(kafkaConsumerLagClient: KafkaConsumerLagClient, config: KafkaConsumerLagClientConfig)

    /**
     * Execute the output task after the ResponseView being initialized
     */
    fun execute()

    /**
     * Identifier for the responseView plugin that is used by the CLI to identify the view
     */
    fun identifier(): String
}