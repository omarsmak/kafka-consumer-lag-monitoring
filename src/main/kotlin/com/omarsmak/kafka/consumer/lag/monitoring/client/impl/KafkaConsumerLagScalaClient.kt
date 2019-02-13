@file:Suppress("MaximumLineLength", "MaxLineLength")
package com.omarsmak.kafka.consumer.lag.monitoring.client.impl

import com.omarsmak.kafka.consumer.lag.monitoring.client.data.Offsets
import com.omarsmak.kafka.consumer.lag.monitoring.client.exceptions.KafkaConsumerLagClientException
import java.util.Properties
import kotlin.collections.HashMap
import kotlin.collections.List
import kotlin.collections.listOf
import kotlin.collections.mutableListOf
import kotlin.collections.set
import kotlin.collections.toMutableList

/**
 * An abstraction over Kafka Scala and Java admin clients
 *
 * @author oalsafi
 * @since 2018-07-16
 */

@Deprecated("This class is deprecated in favor of the com.trivago.up.kafka.consumer.monitoring.client.impl.KafkaOffsetJavaClientImpl client since the Scala client is deprecated in Kafka library.")
internal class KafkaConsumerLagScalaClient private constructor(config: Properties) : KafkaConsumerLagBaseClient(config) {

    // Create all the required clients from Kafka, in this implementation we will use Java and Scala clients
    private val scalaAdminClient = kafka.admin.AdminClient.create(config)

    companion object {
        fun create(config: Properties): KafkaConsumerLagScalaClient {
            return KafkaConsumerLagScalaClient(config)
        }
    }

    override fun getConsumerGroupsList(): List<String> {
        val scalaConsumersList = scalaAdminClient.listAllConsumerGroupsFlattened().mapConserve { it.groupId() }
        if (scalaConsumersList.isEmpty) throw KafkaConsumerLagClientException("No consumers existing in the Kafka cluster.")
        val consumersList = mutableListOf<String>()
        scalaConsumersList.foreach { consumer ->
            consumersList.add(consumer)
        }
        return consumersList.toMutableList()
    }

    override fun getConsumerOffsets(consumerGroup: String): List<Offsets> {
        val offsets = scalaAdminClient.listGroupOffsets(consumerGroup)
        if (offsets == null || offsets.isEmpty) throw KafkaConsumerLagClientException("Consumer group `$consumerGroup` does not exist in the Kafka cluster.")

        val consumerOffsetsMap = HashMap<Int, Long>()
        var topicName = ""
        offsets.foreach { offset ->
            consumerOffsetsMap[offset._1.partition()] = offset._2 as Long
            when (topicName.isEmpty()) {
                true -> topicName = offset._1.topic()
            }
        }
        return listOf(Offsets(topicName, consumerOffsetsMap))
    }

    override fun closeClients() {
        scalaAdminClient.close()
    }
}
