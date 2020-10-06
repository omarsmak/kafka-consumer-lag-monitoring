package com.omarsmak.kafka.consumer.lag.monitoring.component.prometheus

import com.omarsmak.kafka.consumer.lag.monitoring.client.data.Lag
import com.omarsmak.kafka.consumer.lag.monitoring.component.MonitoringComponent

class PrometheusMonitoringComponent : MonitoringComponent{
    override fun configure(configs: Map<String, Any>) {

    }

    override fun start() {

    }

    override fun stop() {

    }

    override fun beforeProcess() {

    }

    override fun process(consumerGroup: String, lag: List<Lag>, memberLag: Map<String, List<Lag>>) {

    }

    override fun afterProcess() {

    }

    override fun identifier(): String = "prometheus"

}