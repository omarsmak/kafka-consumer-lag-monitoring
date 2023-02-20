package com.omarsmak.kafka.consumer.lag.monitoring.component.prometheus

import com.omarsmak.kafka.consumer.lag.monitoring.client.exceptions.KafkaConsumerLagClientException
import com.omarsmak.kafka.consumer.lag.monitoring.component.MonitoringComponent
import com.omarsmak.kafka.consumer.lag.monitoring.data.ConsumerGroupLag
import com.omarsmak.kafka.consumer.lag.monitoring.support.castToInt
import io.prometheus.client.Gauge
import io.prometheus.client.exporter.HTTPServer
import mu.KotlinLogging

class PrometheusMonitoringComponent : MonitoringComponent{

    companion object {
        const val HTTP_PORT = "http.port"
        const val DEFAULT_HTTP_PORT = 9739

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

        private val kafkaConsumerMemberLagGauge = Gauge.build()
                .name("kafka_consumer_group_member_lag")
                .help("The total lag of a consumer group member behind the head of a topic. This gives the total lags over each consumer member within consumer group")
                .labelNames("group", "member", "topic")
                .register()

        private val kafkaConsumerMemberPartitionLagGauge = Gauge.build()
                .name("kafka_consumer_group_member_partition_lag")
                .help("The lag of a consumer member within consumer group behind the head of a given partition of a topic")
                .labelNames("group", "member", "topic", "partition")
                .register()
    }

    private var httpPort = DEFAULT_HTTP_PORT
    private lateinit var httpServer: HTTPServer

    override fun configure(configs: Map<String, Any>) {
        httpPort = configs.getOrDefault(HTTP_PORT, DEFAULT_HTTP_PORT).castToInt()
    }

    override fun start() {
        // Start a HTTP server to expose metrics
        logger.info("Starting HTTP server on $httpPort....")
        httpServer = HTTPServer(httpPort)
    }

    override fun stop() {
        // Stop our HTTP server
        logger.info("Stopping HTTP server on $httpPort....")
        httpServer.stop()
    }

    override fun beforeProcess() {
        // reset metrics before we process a consumer on every polling
        kafkaConsumerGroupOffsetGauge.clear()
        kafkaConsumerLagPerPartitionGauge.clear()
        kafkaTopicLatestOffsetsGauge.clear()
        kafkaConsumerTotalLagGauge.clear()
        kafkaConsumerMemberLagGauge.clear()
    }

    override fun process(consumerGroup: String, consumerGroupLag: ConsumerGroupLag) {
        try {
            val lag = consumerGroupLag.lag
            val memberLag = consumerGroupLag.memberLag

            // Push metrics for each topic
            lag.forEach { topic ->
                // Push kafka_consumer_group_total_lag
                kafkaConsumerTotalLagGauge.labels(consumerGroup, topic.topicName).set(topic.totalLag.toDouble())

                // Push kafka_consumer_group_offset metrics for each partition
                topic.latestConsumerOffsets.forEach { (t, u) ->
                    kafkaConsumerGroupOffsetGauge
                            .pushKafkaMetricsPerPartition(consumerGroup, topic.topicName, t, u.toDouble())
                }

                // Push kafka_topic_latest_offsets metrics for each partition
                topic.latestTopicOffsets.forEach { (t, u) ->
                    kafkaTopicLatestOffsetsGauge
                            .pushKafkaMetricsPerPartition(consumerGroup, topic.topicName, t, u.toDouble())
                }

                // Push kafka_consumer_group_partition_lag metrics for each partition
                topic.lagPerPartition.forEach { (t, u) ->
                    kafkaConsumerLagPerPartitionGauge
                            .pushKafkaMetricsPerPartition(consumerGroup, topic.topicName, t, u.toDouble())
                }
            }

            memberLag.forEach { (member, lags) ->
                lags.forEach {
                    kafkaConsumerMemberLagGauge.labels(consumerGroup, member, it.topicName).set(it.totalLag.toDouble())

                    it.lagPerPartition.forEach { (t, u) ->
                        kafkaConsumerMemberPartitionLagGauge.labels(consumerGroup, member, it.topicName, t.toString()).set(u.toDouble())
                    }
                }
            }
        } catch (e: KafkaConsumerLagClientException) {
            logger.error(e.message, e.cause)
        }
    }

    override fun afterProcess() {
    }

    override fun identifier(): String = "prometheus"

    override fun onError(t: Throwable) {
        logger.error(t.message, t)
    }

    private fun Gauge.pushKafkaMetricsPerPartition(consumer: String, topicName: String, partition: Int, value: Double) {
        this.labels(
                consumer,
                topicName,
                partition.toString()
        ).set(value)
    }
}