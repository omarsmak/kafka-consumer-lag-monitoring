package com.omarsmak.kafka.consumer.lag.monitoring.component.console

import com.github.ajalt.mordant.TermColors
import com.omarsmak.kafka.consumer.lag.monitoring.engine.MonitoringEngine
import com.omarsmak.kafka.consumer.lag.monitoring.support.Utils
import com.omarsmak.kafka.consumer.lag.monitoring.support.asResource
import org.apache.kafka.clients.admin.AdminClientConfig
import picocli.CommandLine
import java.lang.Exception
import java.util.concurrent.Callable

private const val META_DATA_FILE = "application-meta.properties"

@CommandLine.Command(mixinStandardHelpOptions = true, versionProvider = VersionProvider::class,
        description = ["Prints the kafka consumer lag to the console."])
class ClientCli : Callable<Int> {

    @CommandLine.Option(names = ["-b", "--bootstrap.servers"], description = ["A list of host/port pairs to use for establishing the initial connection to the Kafka cluster"])
    var kafkaBootstrapServers: String = ""

    @CommandLine.Option(names = ["-c", "--consumer.groups"], description = ["A list of Kafka consumer groups or list ending with star (*) to fetch all consumers with matching pattern, e.g: 'test_v*'"])
    var kafkaConsumerGroups: String = ""

    @CommandLine.Option(names = ["-p", "--poll.interval"], description = ["Interval delay in ms to that refreshes the client lag metrics, default to 2000ms"])
    var pollInterval: Int = MonitoringEngine.DEFAULT_POLL_INTERVAL


    @CommandLine.Option(names = ["-f", "--properties.file"], description = ["Optional. Properties file for Kafka AdminClient configurations, this is the typical Kafka properties file that can be used in the AdminClient. For more info, please take a look at Kafka AdminClient configurations documentation."])
    var kafkaPropertiesFile: String = ""

    override fun call(): Int {

        // Load config
        val configs = initializeConfigurations(kafkaConsumerGroups)

        // Run the engine

        val engine = MonitoringEngine.createWithComponentAndConfigs(ConsoleMonitoringComponent(), configs)

        try {
            // start our engine
            engine.start()

            // add shutdown hook
            Runtime.getRuntime().addShutdownHook(Thread {
                engine.stop()
            })
        } catch (e: Exception) {
            println(TermColors().red("Error: $e"))
            return 0
        }

        return 0
    }

    private fun initializeConfigurations(targetConsumerGroups: String) = mutableMapOf<String, Any?>(
            "${MonitoringEngine.CONFIG_KAFKA_PREFIX}.${AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG}" to kafkaBootstrapServers,
            "${MonitoringEngine.CONFIG_LAG_CLIENT_PREFIX}.${MonitoringEngine.POLL_INTERVAL}" to pollInterval,
            "${MonitoringEngine.CONFIG_LAG_CLIENT_PREFIX}.${MonitoringEngine.CONSUMER_GROUPS}" to targetConsumerGroups
    ).apply {
        if (kafkaPropertiesFile.isNotEmpty()) {
            putAll(Utils.loadPropertiesFileAsMap(kafkaPropertiesFile))
        }
    }
}

class VersionProvider : CommandLine.IVersionProvider {
    private val meta = Utils.loadPropertiesFromInputStream(META_DATA_FILE.asResource())

    override fun getVersion(): Array<String> = arrayOf(meta["version"] as String)


}

fun main(args: Array<String>) {

    val metadata = Utils.loadPropertiesFromInputStream(META_DATA_FILE.asResource())

    CommandLine(ClientCli())
            .setCommandName(metadata["name"] as String)
            .execute(*args)
}