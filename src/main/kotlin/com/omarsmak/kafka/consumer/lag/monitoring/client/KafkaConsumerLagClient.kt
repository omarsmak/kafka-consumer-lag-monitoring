package com.omarsmak.kafka.consumer.lag.monitoring.client

import com.omarsmak.kafka.consumer.lag.monitoring.client.data.Lag
import com.omarsmak.kafka.consumer.lag.monitoring.client.data.Offsets
import org.apache.kafka.clients.admin.TopicDescription

/**
 * Interface for KafkaOffsetClient public API
 *
 * @author oalsafi
 * @since 2018-07-16
 */

interface KafkaConsumerLagClient : AutoCloseable {

    /**
     * Return topic list
     */
    fun getTopicsList(): List<String>

    /**
     * Return clusters list
     */
    fun getTopicsInfo(topicsCollection: Collection<String>): Map<String, TopicDescription>

    /**
     * Return consumers groups list
     */
    fun getConsumerGroupsList(): List<String>

    /**
     * Return current offsets for a consumer
     */
    fun getConsumerOffsets(consumerGroup: String): List<Offsets>

    /**
     * Return topic offset per partition
     */
    fun getTopicOffsets(topicName: String): Offsets

    /**
     * Return consumer lag per topic, the way to calculate this as follows:
     * lag = current_topic_offset - current_consumer_offset
     */
    fun getConsumerLag(consumerGroup: String): List<Lag>
}
