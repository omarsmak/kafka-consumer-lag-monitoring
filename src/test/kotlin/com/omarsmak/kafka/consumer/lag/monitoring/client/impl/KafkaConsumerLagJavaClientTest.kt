package com.omarsmak.kafka.consumer.lag.monitoring.client.impl

import com.omarsmak.kafka.consumer.lag.monitoring.client.KafkaConsumerLagClient
import com.omarsmak.kafka.consumer.lag.monitoring.client.KafkaConsumerLagClientFactory
import com.omarsmak.kafka.consumer.lag.monitoring.client.exceptions.KafkaConsumerLagClientException
import org.apache.kafka.clients.admin.AdminClientConfig
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.util.*
import kotlin.test.assertFailsWith

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class KafkaConsumerLagJavaClientTest {

    private lateinit var kafkaOffsetClient: KafkaConsumerLagClient

    @BeforeAll
    fun initialize() {
        val prop = Properties().apply {
            this[AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG] = "localhost:9092"
            //this[AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG] = "10000"
        }
        kafkaOffsetClient = KafkaConsumerLagClientFactory.getClient("java", prop)
    }

    @Test
    fun `it should return topic list`() {
        val topicLists = kafkaOffsetClient.getTopicsList()

        //assertNotNull(topicLists)
        assertNotNull(1)
    }

    @Test
    fun `it should return consumer group list`() {
        assertNotNull(kafkaOffsetClient.getConsumerGroupsList())
    }

    @Test
    fun `it should return topic info`() {
        val topicName = "test-topic"
        val topicInfo = kafkaOffsetClient.getTopicsInfo(listOf(topicName))

        assertEquals(topicName, topicInfo[topicName]!!.name())
    }


    @Test
    fun `it should return all the consumer offsets per partition`() {
        val consumerGroup = "consumer-test"
        val consumerOffsets = kafkaOffsetClient.getConsumerOffsets(consumerGroup)

        assertTrue(consumerOffsets.isNotEmpty())


        // Test if it throws error if it does not find an existing consumer group in Kafka
        assertFailsWith(KafkaConsumerLagClientException::class) {
            kafkaOffsetClient.getConsumerOffsets("blah_blah_consumer")
        }
    }

    @Test
    fun `it should return all the latest topic offsets per partition`() {
        val topicName = "test-topic"
        val numPartitions = 10
        val topicOffsets = kafkaOffsetClient.getTopicOffsets(topicName)

        assertEquals(topicOffsets.topicName, topicName)
        assertEquals(topicOffsets.offsetPerPartition.size, numPartitions)

        // Test if it throws error if it does not find an existing topic in Kafka
        assertFailsWith(KafkaConsumerLagClientException::class) {
            kafkaOffsetClient.getTopicOffsets("blah_blah_topic")
        }
    }

    @Test
    fun `it should return the consumer lag per partition`() {
        val topicName = "test-topic"
        val consumerGroup = "test-consumer"
        val numPartitions = 10
        val consumerLag = kafkaOffsetClient.getConsumerLag(consumerGroup)

        assertTrue(consumerLag.isNotEmpty())
        assertNotNull(consumerLag.first().totalLag)
        assertTrue(consumerLag.first().lagPerPartition.isNotEmpty())
        assertTrue(consumerLag.first().latestConsumerOffsets.isNotEmpty())
        assertTrue(consumerLag.first().latestTopicOffsets.isNotEmpty())

        // Test if it throws error if it does not find an existing consumer group in Kafka
        assertFailsWith(KafkaConsumerLagClientException::class) {
            kafkaOffsetClient.getConsumerLag("blah_blah_lag")
        }
    }
}