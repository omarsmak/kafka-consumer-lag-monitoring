package com.omarsmak.kafka.consumer.lag.monitoring.component

import com.omarsmak.kafka.consumer.lag.monitoring.data.ConsumerGroupLag

interface MonitoringComponent {
    /**
     * Configure the monitoring component with supplied configs, the configs are only specific for the component with config prefix
     * e.g: monitoring.lag.datadog.config-1 = test, you will get config-1=test in the Map
     *
     * @param configs provided configs for component from [MonitoringEngine]
     */
    fun configure(configs: Map<String, Any>)

    /**
     * Start our monitoring component process, here we can start anything related to our component, e.g: a HTTP server or
     * a client
     */
    fun start()

    /**
     * Stop our monitoring component process, here you can stop anything that you started with start()
     */
    fun stop()

    /**
     * beforeProcess hook will be called before processing a lag for any consumer
     */
    fun beforeProcess()

    /**
     * process hook will be called upon processing a lag per consumer by providing the following parameters
     *
     * @param consumerGroup the processed consumer group name
     * @param consumerGroupLag the consumer lag per topic of the consumer group
     */
    fun process(consumerGroup : String, consumerGroupLag: ConsumerGroupLag)

    /**
     * afterProcess hook will be called after processing all lags for all consumers
     */
    fun afterProcess()

    /**
     * Identifier for the component that can be used to parse the configs automatically by the context
     */
    fun identifier(): String

    /**
     * Error hook to be called on error while the client is polling the data
     */
    fun onError(t: Throwable)
}