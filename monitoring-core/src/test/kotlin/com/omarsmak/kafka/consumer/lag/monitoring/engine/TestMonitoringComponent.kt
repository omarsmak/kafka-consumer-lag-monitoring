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

    override fun process(consumerGroup: String, consumerGroupLag: ConsumerGroupLag) {
        println("$consumerGroup $consumerGroupLag")
    }

    override fun afterProcess() {
    }

    override fun identifier(): String = "test.component"

    override fun onError(t: Throwable) {
        println(t)
    }
}