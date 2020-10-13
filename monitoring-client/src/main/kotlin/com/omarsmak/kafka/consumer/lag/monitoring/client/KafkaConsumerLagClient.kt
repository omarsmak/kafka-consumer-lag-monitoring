package com.omarsmak.kafka.consumer.lag.monitoring.client

import com.omarsmak.kafka.consumer.lag.monitoring.client.data.Lag
import com.omarsmak.kafka.consumer.lag.monitoring.client.data.Offsets

/**
 * Interface for KafkaOffsetClient public API
 *
 * @author oalsafi
 */

interface KafkaConsumerLagClient : AutoCloseable {
    /**
     * Return consumers groups list
     */
    fun getConsumerGroupsList(): List<String>

    /**
     * Return current offsets for a consumer
     */
    fun getConsumerOffsets(consumerGroup: String): List<Offsets>

    /**
     * Return current offsets for consumer members of a consumer group
     */
    fun getConsumerGroupMembersOffsets(consumerGroup: String): Map<String, List<Offsets>>

    /**
     * Return topic offset per partition
     */
    fun getTopicOffsets(topicName: String): Offsets

    /**
     * Return consumer lag per topic, the way to calculate this as follows:
     * lag = current_topic_offset - current_consumer_offset
     */
    fun getConsumerLag(consumerGroup: String): List<Lag>

    /**
     * Return consumer lag per member per topic, the way to calculate this as follows:
     * lag = current_topic_offset - current_consumer_offset
     */
    fun getConsumerMemberLag(consumerGroup: String): Map<String, List<Lag>>
}
