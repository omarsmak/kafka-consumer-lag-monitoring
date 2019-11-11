@file:Suppress("MaximumLineLength", "MaxLineLength")

package com.omarsmak.kafka.consumer.lag.monitoring.response

import com.omarsmak.kafka.consumer.lag.monitoring.client.KafkaConsumerLagClient
import com.omarsmak.kafka.consumer.lag.monitoring.client.exceptions.KafkaConsumerLagClientException
import com.omarsmak.kafka.consumer.lag.monitoring.config.KafkaConsumerLagClientConfig
import io.prometheus.client.Gauge
import io.prometheus.client.exporter.HTTPServer
import mu.KotlinLogging
import java.util.*
import kotlin.concurrent.scheduleAtFixedRate

/**
 * This class publishes the lag out as prometheus metrics
 */
class PrometheusResponseView : ResponseView {

    companion object {
        private val logger = KotlinLogging.logger {}

        private val kafkaConsumerGroupOffsetGauge = Gauge.build()
                .name("kafka_consumer_group_offset")
                .help("The latest committed offset of a consumer group in a given partition of a topic")
                .labelNames("group", "topic", "partition")
                .register()

        private val kafkaConsumerLagPerPartitionGauge = Gauge.build()
                .name("kafka_consumer_group_partition_lag")
                .help("The lag of a consumer group behind the head of a given partition of a topic. Calculated like this: current_topic_offset_per_partition - current_consumer_offset_per_partition")
                .labelNames("group", "topic", "partition")
                .register()

        private val kafkaTopicLatestOffsetsGauge = Gauge.build()
                .name("kafka_topic_latest_offsets")
                .help("The latest committed offset of a topic in a given partition")
                .labelNames("group", "topic", "partition")
                .register()

        private val kafkaConsumerTotalLagGauge = Gauge.build()
                .name("kafka_consumer_group_total_lag")
                .help("The total lag of a consumer group behind the head of a topic. This gives the total lags over each partition, it provides good visibility but not a precise measurement since is not partition aware")
                .labelNames("group", "topic")
                .register()

        private fun startServer(port: Int) {
            // Start a HTTP server to expose metrics
            logger.info("Starting HTTP server on $port....")
            HTTPServer(port)
        }
    }

    private lateinit var kafkaConsumerLagClient: KafkaConsumerLagClient
    private lateinit var kafkaConsumerLagClientConfig: KafkaConsumerLagClientConfig

    override fun configure(kafkaConsumerLagClient: KafkaConsumerLagClient, config: KafkaConsumerLagClientConfig) {
        this.kafkaConsumerLagClient = kafkaConsumerLagClient
        this.kafkaConsumerLagClientConfig = config
    }

    override fun execute() {
        val targetConsumerGroups: Set<String> = kafkaConsumerLagClientConfig[KafkaConsumerLagClientConfig.CONSUMER_GROUPS]
        val monitoringPollInterval: Long = kafkaConsumerLagClientConfig[KafkaConsumerLagClientConfig.POLL_INTERVAL]
        val httpPort: Int = kafkaConsumerLagClientConfig[KafkaConsumerLagClientConfig.HTTP_PORT]

        initialize(kafkaConsumerLagClient, targetConsumerGroups, httpPort, monitoringPollInterval)
    }

    override fun identifier(): String = "prometheus"

    /**
     * Start a HTTP server and expose the following PrometheusResponseView metrics:
     * `kafka_consumer_group_offset{group, topic, partition}`
     * `kafka_consumer_group_partition_lag{group, topic, partition}`
     * `kafka_consumer_group_total_lag{group, topic}`
     * `kafka_topic_latest_offsets{group, topic, partition}
     */
    private fun initialize(client: KafkaConsumerLagClient, targetConsumerGroups: Set<String>, port: Int, monitoringPollInterval: Long) {
        // Start HTTP our server
        startServer(port)

        logger.info("Updating metrics every $monitoringPollInterval...")

        // Start publishing our metrics
        Timer().scheduleAtFixedRate(0, monitoringPollInterval) {
            targetConsumerGroups.forEach { consumer ->
                try {
                    val lag = client.getConsumerLag(consumer)

                    // Push metrics for each topic
                    lag.forEach { topic ->
                        // Push kafka_consumer_group_total_lag
                        kafkaConsumerTotalLagGauge.labels(consumer, topic.topicName).set(topic.totalLag.toDouble())

                        // Push kafka_consumer_group_offset metrics for each partition
                        topic.latestConsumerOffsets.forEach { (t, u) ->
                            kafkaConsumerGroupOffsetGauge
                                    .pushKafkaMetricsPerPartition(consumer, topic.topicName, t, u.toDouble())
                        }

                        // Push kafka_topic_latest_offsets metrics for each partition
                        topic.latestTopicOffsets.forEach { (t, u) ->
                            kafkaTopicLatestOffsetsGauge
                                    .pushKafkaMetricsPerPartition(consumer, topic.topicName, t, u.toDouble())
                        }

                        // Push kafka_consumer_group_partition_lag metrics for each partition
                        topic.lagPerPartition.forEach { (t, u) ->
                            kafkaConsumerLagPerPartitionGauge
                                    .pushKafkaMetricsPerPartition(consumer, topic.topicName, t, u.toDouble())
                        }
                    }
                } catch (e: KafkaConsumerLagClientException) {
                    logger.error(e.message, e.cause)
                }
            }
        }
    }

    private fun Gauge.pushKafkaMetricsPerPartition(consumer: String, topicName: String, partition: Int, value: Double) {
        this.labels(
                consumer,
                topicName,
                partition.toString()
        ).set(value)
    }
}
