package com.omarsmak.kafka.consumer.lag.monitoring.component.prometheus

import com.omarsmak.kafka.consumer.lag.monitoring.engine.MonitoringEngine
import com.omarsmak.kafka.consumer.lag.monitoring.support.Utils
import mu.KotlinLogging
import java.lang.Exception

fun main(arg: Array<String>) {
    val configs = Utils.getConfigsFromPropertiesFileOrFromEnv(arg)

    // start our logging service
    Utils.initializeLog4jLoggingWithConfigs(configs)

    val component = PrometheusMonitoringComponent()
    val engine = MonitoringEngine.createWithComponentAndConfigs(component, configs)

    try {
        // start our engine
        engine.start()

        // add shutdown hook
        Runtime.getRuntime().addShutdownHook(Thread {
            engine.stop()
        })
    } catch (e: Exception) {
        KotlinLogging.logger{}.error(e.message, e)
    }
}
