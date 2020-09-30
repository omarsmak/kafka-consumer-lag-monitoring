package com.omarsmak.kafka.consumer.lag.monitoring.client.exceptions

/**
 * Indicates some error while performing an operation
 *
 * @author oalsafi
 */
open class KafkaConsumerLagClientException : RuntimeException {
    constructor(message: String, cause: Throwable) : super(message, cause)

    constructor(message: String) : super(message)

    constructor(cause: Throwable) : super(cause)

    constructor() : super()
}
