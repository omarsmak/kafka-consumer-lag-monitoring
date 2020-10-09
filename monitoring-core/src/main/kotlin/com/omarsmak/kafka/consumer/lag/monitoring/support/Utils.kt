package com.omarsmak.kafka.consumer.lag.monitoring.support

import com.omarsmak.kafka.consumer.lag.monitoring.client.KafkaConsumerLagClient
import com.omarsmak.kafka.consumer.lag.monitoring.engine.MonitoringEngine
import mu.KotlinLogging
import org.apache.logging.log4j.core.config.Configurator
import org.apache.logging.log4j.core.config.properties.PropertiesConfigurationBuilder
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.util.Properties

object Utils {

    const val DEFAULT_LOGGING_FILE = "log4j2.template"
    const val DEFAULT_LOGGING_PREFIX = "${MonitoringEngine.CONFIG_LAG_CLIENT_PREFIX}.logging"

    fun getTargetConsumerGroups(client: KafkaConsumerLagClient, configConsumerGroups: List<String>): Set<String> {
        // Get consumer groups from the kafka broker
        val consumerGroups = client.getConsumerGroupsList()

        // Fetch consumers based no a regex
        val matchedConsumersGroups = configConsumerGroups
                .filter { it.contains("*") }
                .map { x ->
                    consumerGroups.filter { it.startsWith(x.replace("*", "")) }
                }
                .flatten()

        return configConsumerGroups
                .filterNot { it.contains("*") }
                .union(matchedConsumersGroups)
    }

    fun loadPropertiesFile(filePath: String): Properties {
        val inputStream: FileInputStream? = File(filePath).inputStream()
        val prop = Properties()

        if (inputStream == null) {
            throw FileNotFoundException("File '$filePath' not found!")
        }

        prop.load(inputStream)

        return prop
    }

    fun loadPropertiesFileAsMap(filePath: String): Map<String, Any?> {
        val props = loadPropertiesFile(filePath)

        return props.toMap()
                .mapKeys { it.key as String }
    }

    fun getConfigsFromPropertiesFileOrFromEnv(arg: Array<String>): Map<String, Any?> {
        // first we try to load the from properties file using provided args
        // if we fail to find something, we use the env variables as fall back
        return if (arg.isNotEmpty()) loadPropertiesFileAsMap(arg[0]) else System.getenv()
    }

    fun getConfigsWithPrefixCaseInSensitive(configs: Map<String, Any?>, prefix: String): Map<String, Any> {
        val parsedConfigs = configs.mapKeys { it.key.toLowerCase().replace("_", ".") }

        return getConfigsWithPrefix(parsedConfigs, prefix)
    }

    fun getConfigsWithPrefixCaseSensitive(configs: Map<String, Any?>, prefix: String): Map<String, Any> {
        val parsedConfigs = configs.mapKeys { it.key.replace("_", ".") }

        return getConfigsWithPrefix(parsedConfigs, prefix)
    }

    private fun getConfigsWithPrefix(configs: Map<String, Any?>, prefix: String): Map<String, Any> = configs
            .filter {it.key.startsWith(prefix, true) && it.value != null}
            .mapKeys { it.key.replace("$prefix.", "", true) }
            .mapValues { it.value as Any }

    fun initializeLoggingWithConfigs(configs: Map<String, Any?>, prefix: String = DEFAULT_LOGGING_PREFIX) {
        val userConfigs = getConfigsWithPrefixCaseSensitive(configs, prefix)
        val defaultLoggingConfig = loadPropertiesFile(DEFAULT_LOGGING_FILE.asResource().path)

        defaultLoggingConfig.putAll(userConfigs)

        val defaultLoggingConfigAsMap = defaultLoggingConfig.toMap()

        Configurator.initialize(PropertiesConfigurationBuilder().setRootProperties(defaultLoggingConfig).build())

        KotlinLogging.logger{}.info("Logging Configs: $defaultLoggingConfigAsMap")
    }
}
