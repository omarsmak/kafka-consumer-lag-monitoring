package com.omarsmak.kafka.consumer.lag.monitoring.client.impl

import com.omarsmak.kafka.consumer.lag.monitoring.client.KafkaConsumerLagClient
import com.omarsmak.kafka.consumer.lag.monitoring.client.KafkaConsumerLagClientFactory
import com.omarsmak.kafka.consumer.lag.monitoring.client.exceptions.KafkaConsumerLagClientException
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkClass
import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.admin.AdminClientConfig
import org.apache.kafka.clients.admin.ConsumerGroupListing
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.consumer.OffsetAndMetadata
import org.apache.kafka.common.Node
import org.apache.kafka.common.PartitionInfo
import org.apache.kafka.common.TopicPartition
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.util.*
import kotlin.random.Random
import kotlin.test.assertFailsWith

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class KafkaConsumerLagJavaClientTest {

    private val TEST_TOPIC_NAME = "test-topic"
    private val INVALID_TOPIC_NAME = "blah_blah_topic"
    private val TEST_CONSUMER_NAME = "consumer-test"
    private val INVALID_TEST_CONSUMER_NAME = "blah_blah_consumer"

    private lateinit var kafkaOffsetClient: KafkaConsumerLagClient

    // Mocks
    private lateinit var adminClient: AdminClient
    private lateinit var kafkaConsumerClient: KafkaConsumer<String, String>

    @BeforeAll
    @Suppress("UNCHECKED_CAST")
    fun initialize() {
        // Mocking classes
        adminClient = mockkClass(AdminClient::class)
        kafkaConsumerClient = mockkClass(KafkaConsumer::class) as KafkaConsumer<String, String>

        mockClasses()

        // Initialize admin client with injected mocks
        kafkaOffsetClient = KafkaConsumerLagJavaClient(adminClient, kafkaConsumerClient)
    }

    private fun mockClasses(){
        every { adminClient.listConsumerGroups().all().get() } returns mockGroupListings(10)

        every { adminClient.listConsumerGroupOffsets(TEST_CONSUMER_NAME)
                .partitionsToOffsetAndMetadata()
                .get() } returns mapOf(mockTopicPartition(0, TEST_TOPIC_NAME).first() to OffsetAndMetadata(1L))

        every { adminClient.listConsumerGroupOffsets(INVALID_TEST_CONSUMER_NAME)
                .partitionsToOffsetAndMetadata()
                .get() } returns null

        every { kafkaConsumerClient.partitionsFor(TEST_TOPIC_NAME) } returns mockListPartitionInfo(9, TEST_TOPIC_NAME)
        every { kafkaConsumerClient.partitionsFor(INVALID_TOPIC_NAME) } returns null
        every { kafkaConsumerClient.endOffsets(mockTopicPartition(9, TEST_TOPIC_NAME))} returns mockTopicPartitionToEndOffset(9, TEST_TOPIC_NAME)
    }

    private fun mockGroupListings(numOfGroups: Int): List<ConsumerGroupListing> {
        val resultList = mutableListOf<ConsumerGroupListing>()
        for (i in 0 .. numOfGroups){
            resultList.add(ConsumerGroupListing("group-$i", true))
        }
        return resultList
    }

    private fun mockTopicPartition(numPartition: Int, topicName: String): List<TopicPartition> {
        val resultList = mutableListOf<TopicPartition>()
        for (i in 0 .. numPartition){
            resultList.add(TopicPartition(topicName, i))
        }
        return resultList
    }

    private fun mockListPartitionInfo(numPartition: Int, topicName: String): List<PartitionInfo> {
        val resultList = mutableListOf<PartitionInfo>()
        for (i in 0 .. numPartition) {
            resultList.add(PartitionInfo(
                    topicName,
                    i,
                    mockArrayNodes().first(),
                    mockArrayNodes(),
                    mockArrayNodes()
            ))
        }
        return resultList
    }

    private fun mockArrayNodes() = arrayOf( Node(1,"node-1",1234),  Node(2,"node-2",1234))

    private fun mockTopicPartitionToEndOffset(numPartition: Int, topicName: String): Map<TopicPartition, Long> {
        val topicPartition = mockTopicPartition(numPartition, topicName)
        return topicPartition.map {
            it to Random.nextLong()
        }.toMap()
    }

    @Test
    fun `it should return all consumer groups`(){
        val consumerGroups = kafkaOffsetClient.getConsumerGroupsList()
        assertTrue(consumerGroups.isNotEmpty())
    }

    @Test
    fun `it should return all the consumer offsets per partition`() {
        val consumerOffsets = kafkaOffsetClient.getConsumerOffsets(TEST_CONSUMER_NAME)

        assertTrue(consumerOffsets.isNotEmpty())

        // Test if it throws error if it does not find an existing consumer group in Kafka
        assertFailsWith(KafkaConsumerLagClientException::class) {
            kafkaOffsetClient.getConsumerOffsets(INVALID_TEST_CONSUMER_NAME)
        }
    }

    @Test
    fun `it should return all the latest topic offsets per partition`() {
        val numPartitions = 10
        val topicOffsets = kafkaOffsetClient.getTopicOffsets(TEST_TOPIC_NAME)

        assertEquals(topicOffsets.topicName, TEST_TOPIC_NAME)
        assertEquals(topicOffsets.offsetPerPartition.size, numPartitions)

        // Test if it throws error if it does not find an existing topic in Kafka
        assertFailsWith(KafkaConsumerLagClientException::class) {
            kafkaOffsetClient.getTopicOffsets(INVALID_TOPIC_NAME)
        }
    }

    @Test
    fun `it should return the consumer lag per partition`() {
        val consumerLag = kafkaOffsetClient.getConsumerLag(TEST_CONSUMER_NAME)

        assertTrue(consumerLag.isNotEmpty())
        assertNotNull(consumerLag.first().totalLag)
        assertTrue(consumerLag.first().lagPerPartition.isNotEmpty())
        assertTrue(consumerLag.first().latestConsumerOffsets.isNotEmpty())
        assertTrue(consumerLag.first().latestTopicOffsets.isNotEmpty())

        // Test if it throws error if it does not find an existing consumer group in Kafka
        assertFailsWith(KafkaConsumerLagClientException::class) {
            kafkaOffsetClient.getConsumerLag(INVALID_TEST_CONSUMER_NAME)
        }
    }
}