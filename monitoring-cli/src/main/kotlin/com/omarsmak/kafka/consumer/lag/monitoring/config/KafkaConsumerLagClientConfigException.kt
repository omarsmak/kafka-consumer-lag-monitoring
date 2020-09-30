package com.omarsmak.kafka.consumer.lag.monitoring.config

import com.omarsmak.kafka.consumer.lag.monitoring.client.exceptions.KafkaConsumerLagClientException

/**
 * Indicates some error while performing a config operation
 *
 * @author oalsafi
 */
open class KafkaConsumerLagClientConfigException : KafkaConsumerLagClientException {
    constructor(message: String, cause: Throwable) : super(message, cause)

    constructor(message: String) : super(message)

    constructor(cause: Throwable) : super(cause)

    constructor() : super()
}
