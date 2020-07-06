@file:Suppress("MaximumLineLength", "MaxLineLength")

package com.omarsmak.kafka.consumer.lag.monitoring.client.impl

import com.omarsmak.kafka.consumer.lag.monitoring.client.data.Offsets
import com.omarsmak.kafka.consumer.lag.monitoring.client.exceptions.KafkaConsumerLagClientException
import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.admin.ListConsumerGroupOffsetsOptions
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.consumer.OffsetAndMetadata
import org.apache.kafka.common.TopicPartition

/**
 * An abstraction over Kafka Java clients
 *
 * @author oalsafi
 */

internal class KafkaConsumerLagJavaClient(
        private val javaAdminClient: AdminClient,
        kafkaConsumerClient: KafkaConsumer<String, String>
) : AbstractKafkaConsumerLagClient(kafkaConsumerClient) {

    override fun getConsumerGroupsList(): List<String> {
        val consumerList = javaAdminClient.listConsumerGroups().all().get().map { it.groupId() }
        if (consumerList.isEmpty()) throw KafkaConsumerLagClientException("No consumers existing in the Kafka cluster.")
        return consumerList
    }

    override fun getConsumerOffsets(consumerGroup: String): List<Offsets> {
        val offsets = javaAdminClient.listConsumerGroupOffsets(consumerGroup)
                .partitionsToOffsetAndMetadata()
                .get()
                ?: throw KafkaConsumerLagClientException("Consumer group `$consumerGroup` does not exist in the Kafka cluster.")

        return getConsumerOffsetsPerTopic(offsets)
    }

    override fun getConsumerGroupMembersOffsets(consumerGroup: String): Map<String, List<Offsets>> {
        // to get offsets per member, we need to do the following:
        // 1. fetch consumer members for a consumer group
        // 2. fetch group offsets for consumer members once we have the topic partition
        val consumerGroupMembersEnvelope = javaAdminClient.describeConsumerGroups(listOf(consumerGroup))
                .all()
                .get()
                ?: throw KafkaConsumerLagClientException("Consumer group `$consumerGroup` does not exist in the Kafka cluster.")

        val consumerGroupMembers = consumerGroupMembersEnvelope.values.firstOrNull()
                ?: throw KafkaConsumerLagClientException("Consumer group `$consumerGroup` and its members does not exist in the Kafka cluster.")

        return consumerGroupMembers
                .members()
                .map { it.consumerId() to getConsumerOffsetPerMember(consumerGroup, it.assignment().topicPartitions().toList()) }
                .toMap()
    }

    private fun getConsumerOffsetPerMember(consumerGroup: String, topicPartition: List<TopicPartition>): List<Offsets> {
        val offsets = javaAdminClient.listConsumerGroupOffsets(consumerGroup, ListConsumerGroupOffsetsOptions().topicPartitions(topicPartition))
                .partitionsToOffsetAndMetadata()
                .get()
                ?: throw KafkaConsumerLagClientException("Consumer group `$consumerGroup` does not exist in the Kafka cluster.")

        return getConsumerOffsetsPerTopic(offsets)
    }

    private fun getConsumerOffsetsPerTopic(offsets: Map<TopicPartition, OffsetAndMetadata?>): List<Offsets> {
        val rawOffsets = mutableMapOf<String, MutableMap<Int, Long>>()
        offsets.filterValues { it != null }
                .forEach { (t, u) ->
                    // First we get the key of the topic
                    val offsetPerPartition = rawOffsets.getOrPut(t.topic()) { mutableMapOf() }
                    // Add the updated map
                    offsetPerPartition.putIfAbsent(t.partition(), u!!.offset())
                    rawOffsets.replace(t.topic(), offsetPerPartition)
                }

        return rawOffsets.map {
            Offsets(it.key, it.value)
        }
    }


    override fun closeClients() {
        javaAdminClient.close()
    }
}
