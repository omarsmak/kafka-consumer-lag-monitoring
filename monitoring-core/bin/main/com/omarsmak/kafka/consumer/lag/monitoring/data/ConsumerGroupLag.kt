package com.omarsmak.kafka.consumer.lag.monitoring.data

import com.omarsmak.kafka.consumer.lag.monitoring.client.data.Lag

data class ConsumerGroupLag(
        /**
         * the name of the consumer group where the lag belongs to
         */
        val consumerGroup: String,

        /**
         * the consumer lag per topic of the consumer group, hence the list of lags here correspond to list of topics
         * whereby one lag represents one topic that group assigned to
         */
        val lag: List<Lag>,

        /**
         * the member lag per member per topic of the consumer group, here the key is the consumer group member and the list of lags
         * per topic
         */
        val memberLag: Map<String, List<Lag>>
)