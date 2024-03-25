package com.omarsmak.kafka.consumer.lag.monitoring.support

import com.omarsmak.kafka.consumer.lag.monitoring.client.KafkaConsumerLagClient
import com.omarsmak.kafka.consumer.lag.monitoring.engine.MonitoringEngine
import mu.KotlinLogging
import org.apache.logging.log4j.core.config.Configurator
import org.apache.logging.log4j.core.config.properties.PropertiesConfigurationBuilder
import java.io.*
import java.util.Properties

object Utils {

    const val DEFAULT_LOGGING_FILE = "config/log4j2.template"
    const val DEFAULT_LOGGING_PREFIX = "${MonitoringEngine.CONFIG_LAG_CLIENT_PREFIX}.logging"

    fun getTargetConsumerGroups(client: KafkaConsumerLagClient, configConsumerGroups: List<String>, configExcludedConsumerGroups: List<String>): Set<String> {
        // Get consumer groups from the kafka broker
        val consumerGroups = client.getConsumerGroupsList()

        // Fetch consumers based no a regex
        val matchedConsumersGroups = configConsumerGroups
                .filter { it.contains("*") }
                .map { x ->
                    consumerGroups.filter {
                      if (x.startsWith("*") && x.endsWith("*"))
                        it.contains(x.replace("*", ""))
                      else if (x.endsWith("*"))
                        it.startsWith(x.replace("*", ""))
                      else if (x.startsWith("*"))
                        it.endsWith(x.replace("*", ""))
                      else
                        false
                    }
                }
                .flatten()
                .union(
                  configConsumerGroups
                          .filterNot { it.contains("*") }
                )

        val exludedConsumersGroups = configExcludedConsumerGroups
                .filter { it.contains("*") }
                .map { x ->
                    consumerGroups.filter {
                      if (x.startsWith("*") && x.endsWith("*"))
                        it.contains(x.replace("*", ""))
                      else if (x.endsWith("*"))
                        it.startsWith(x.replace("*", ""))
                      else if (x.startsWith("*"))
                        it.endsWith(x.replace("*", ""))
                      else
                        false
                    }
                }
                .flatten()
                .union(
                  configExcludedConsumerGroups
                          .filterNot { it.contains("*") }
                )

        return matchedConsumersGroups.minus(exludedConsumersGroups)
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

    fun loadPropertiesFromInputStream(inputStream: InputStream): Properties {
        val prop = Properties()

        prop.load(inputStream)

        return prop
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
            .filter { it.key.startsWith(prefix, true) && it.value != null }
            .mapKeys { it.key.replace("$prefix.", "", true) }
            .mapValues { it.value as Any }

    /**
     * The will initialize log4j configs, however if you wish to use this, you will need to make sure that you have
     * log4j-slf4j-impl in the class path
     */
    fun initializeLog4jLoggingWithConfigs(configs: Map<String, Any?>, prefix: String = DEFAULT_LOGGING_PREFIX) {
        val userConfigs = getConfigsWithPrefixCaseSensitive(configs, prefix)
        val defaultLoggingConfig = loadPropertiesFromInputStream(DEFAULT_LOGGING_FILE.asResource())

        defaultLoggingConfig.putAll(userConfigs)

        val defaultLoggingConfigAsMap = defaultLoggingConfig.toMap()

        Configurator.initialize(PropertiesConfigurationBuilder().setRootProperties(defaultLoggingConfig).build())

        KotlinLogging.logger {}.info("Logging Configs: $defaultLoggingConfigAsMap")
    }

    fun updateAndTrackConsumerGroups(trackedConsumerGroups: Set<String>, sourceConsumerGroups: Set<String>, keepGroups: Boolean = true): DiffConsumerGroups {
        // first check if we have new consumer groups
        val newGroups = sourceConsumerGroups.minus(trackedConsumerGroups)

        // second check if we have removed groups
        val removedGroups = trackedConsumerGroups.minus(sourceConsumerGroups)

        // update our trackedConsumerGroups
        if (keepGroups) {
            return DiffConsumerGroups(newGroups, removedGroups, trackedConsumerGroups
                    .plus(newGroups))
        }

        // update our trackedConsumerGroups
        return DiffConsumerGroups(newGroups, removedGroups, trackedConsumerGroups
                .plus(newGroups).minus(removedGroups))
    }

    data class DiffConsumerGroups(
            val newGroups: Set<String>,
            val removedGroups: Set<String>,
            val updatedConsumerGroups: Set<String>
    )
}
