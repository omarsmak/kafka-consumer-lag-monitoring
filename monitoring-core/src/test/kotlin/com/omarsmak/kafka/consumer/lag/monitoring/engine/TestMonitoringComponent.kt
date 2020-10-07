package com.omarsmak.kafka.consumer.lag.monitoring.engine

import com.omarsmak.kafka.consumer.lag.monitoring.component.MonitoringComponent
import com.omarsmak.kafka.consumer.lag.monitoring.data.ConsumerGroupLag

class TestMonitoringComponent: MonitoringComponent {
    override fun configure(configs: Map<String, Any>) {
        println(configs)
    }

    override fun start() {
    }

    override fun stop() {
    }

    override fun beforeProcess() {
    }

    override fun process(consumerGroup: String, lag: ConsumerGroupLag) {
        println("$consumerGroup $lag")
    }

    override fun afterProcess() {
    }

    override fun identifier(): String = "test.component"
}