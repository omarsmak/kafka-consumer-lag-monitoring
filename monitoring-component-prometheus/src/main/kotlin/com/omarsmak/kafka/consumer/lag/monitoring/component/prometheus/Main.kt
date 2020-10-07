package com.omarsmak.kafka.consumer.lag.monitoring.component.prometheus

import com.omarsmak.kafka.consumer.lag.monitoring.engine.MonitoringEngine
import com.omarsmak.kafka.consumer.lag.monitoring.support.Utils

fun main(arg: Array<String>) {
    val configs = Utils.getConfigsFromPropertiesFileOrFromEnv(arg)
    val component = PrometheusMonitoringComponent()
    val engine = MonitoringEngine.createWithComponentAndConfigs(component, configs)

    // start our engine
    engine.start()

    // add shutdown hook
    Runtime.getRuntime().addShutdownHook(Thread {
        engine.stop()
    })
}
