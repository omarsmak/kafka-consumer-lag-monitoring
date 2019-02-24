@file:Suppress("MaximumLineLength", "MaxLineLength")

package com.omarsmak.kafka.consumer.lag.monitoring.client.impl

import com.omarsmak.kafka.consumer.lag.monitoring.client.KafkaConsumerLagClient
import com.omarsmak.kafka.consumer.lag.monitoring.client.data.Lag
import com.omarsmak.kafka.consumer.lag.monitoring.client.data.Offsets
import com.omarsmak.kafka.consumer.lag.monitoring.client.exceptions.KafkaConsumerLagClientException
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.TopicPartition

/**
 * Base client class
 *
 * @author oalsafi
 */

internal abstract class AbstractKafkaConsumerLagClient(
    private val kafkaConsumerClient: KafkaConsumer<String, String>
) : KafkaConsumerLagClient {

    protected abstract fun closeClients()

    override fun getTopicOffsets(topicName: String): Offsets {
        val partitions = kafkaConsumerClient.partitionsFor(topicName).orEmpty()
        if (partitions.isEmpty()) throw KafkaConsumerLagClientException("Topic `$topicName` does not exist in the Kafka cluster.")
        val topicPartition = partitions.map {
            TopicPartition(it.topic(), it.partition())
        }

        val topicOffsetsMap = kafkaConsumerClient.endOffsets(topicPartition).map {
            it.key.partition() to it.value
        }.toMap()

        return Offsets(topicName, topicOffsetsMap)
    }

    override fun getConsumerLag(consumerGroup: String): List<Lag> {
        val consumerOffsets = getConsumerOffsets(consumerGroup)
        return consumerOffsets.map {
            getConsumerLagPerTopic(it)
        }
    }

    override fun close() {
        kafkaConsumerClient.wakeup()
        closeClients()
    }

    private fun getConsumerLagPerTopic(consumerOffsets: Offsets): Lag {
        val topicOffsets = getTopicOffsets(consumerOffsets.topicName)

        val lagPerPartitionAndTotalLag = calculateLagPerPartitionAndTotalLag(topicOffsets, consumerOffsets)
        val lagPerPartition = lagPerPartitionAndTotalLag.first
        val totalLag = lagPerPartitionAndTotalLag.second

        return Lag(
                topicOffsets.topicName,
                totalLag,
                lagPerPartition,
                topicOffsets.offsetPerPartition,
                consumerOffsets.offsetPerPartition
        )
    }

    private fun calculateLagPerPartitionAndTotalLag(topicOffsets: Offsets, consumerOffsets: Offsets): Pair<Map<Int, Long>, Long>{
        var totalLag = 0L
        val lagPerPartition = consumerOffsets.offsetPerPartition.map { (k, v) ->
            val lag = topicOffsets.offsetPerPartition.getValue(k) - v
            totalLag += lag
            k to lag
        }.toMap()

        return (lagPerPartition to totalLag)
    }
}
