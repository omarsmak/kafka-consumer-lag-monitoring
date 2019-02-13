package com.omarsmak.kafka.consumer.lag.monitoring.client.data

/**
 * Entities representations
 *
 * @author oalsafi
 * @since 2018-07-21
 */

data class Offsets(
    val topicName: String,
    val offsetPerPartition: Map<Int, Long>
)

data class Lag(
    val topicName: String,
    val totalLag: Long,
    val lagPerPartition: Map<Int, Long>,
    val latestTopicOffsets: Map<Int, Long>,
    val latestConsumerOffsets: Map<Int, Long>
)
